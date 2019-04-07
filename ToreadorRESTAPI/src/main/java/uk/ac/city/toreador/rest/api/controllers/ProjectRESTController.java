package uk.ac.city.toreador.rest.api.controllers;

import com.google.common.io.Files;
import javafx.scene.shape.PathElement;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sun.security.krb5.internal.PAData;
import uk.ac.city.toreador.rest.api.everest.entities.Event;
import uk.ac.city.toreador.rest.api.everest.entities.EventParameter;
import uk.ac.city.toreador.rest.api.everest.entities.TemplateDefined;
import uk.ac.city.toreador.rest.api.everest.repositories.EventRepository;
import uk.ac.city.toreador.rest.api.everest.repositories.TemplateDefinedRepository;
import uk.ac.city.toreador.rest.api.everest.repositories.TemplateRepository;
import uk.ac.city.toreador.rest.api.toreador.entities.*;
import uk.ac.city.toreador.rest.api.toreador.repositories.*;
import uk.ac.city.toreador.validation.Translate;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQStaticContext;
import java.awt.*;
import java.io.*;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class ProjectRESTController {

    final static Logger log = Logger.getLogger(ProjectRESTController.class);

    @Autowired
    JdbcTemplate template;

    @Autowired
    ProjectsRepository projectsRepository;

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    CompositeServicesRepository compositeServicesRepository;

    @Autowired
    AtomicServicesRepository atomicServicesRepository;

    @Autowired
    AssetsRepository assetsRepository;

    @Autowired
    OperationsRepository operationsRepository;

    @Autowired
    OutputsRepository outputsRepository;

    @Autowired
    InputsRepository inputsRepository;

    @Autowired
    GuardedActionsRepository guardedActionsRepository;

    @Autowired
    GuarateeTermsRepository guarateeTermsRepository;

    @Autowired
    AssetSecurityPropertyPairsRepository assetSecurityPropertyPairsRepository;

    @Autowired
    NegotiationsRepository negotiationsRepository;

    @Autowired
    NegotiationRequestRepository negotiationRequestRepository;

    @Autowired
    SecurityPropertiesRepository securityPropertiesRepository;

    @Autowired
    SlosRepository slosRepository;

    @Autowired
    SloTemplatesRepository sloTemplatesRepository;

    @Autowired
    VelocityEngine engine;

    @Autowired
    ExplorationsRepository explorationsRepository;

    @Autowired
    TemplateRepository templateRepository;

    @Autowired
    TemplateDefinedRepository templateDefinedRepository;

    @Autowired
    EventRepository eventRepository;

    @Transactional
    @RequestMapping(value = "/rest/api/users/{uid}/projects", method = RequestMethod.POST)
    public ResponseEntity<Project> createProject(
            @PathVariable Integer uid,
            @RequestBody Project project) {

        try {
            User user = usersRepository.findOne(uid);
            if (user == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                project.setCreated(new Timestamp(System.currentTimeMillis()));
                project.setStatus(ProjectStatus.CREATING);
                project.setUser(user);
                project.setEventChannel(null);
                projectsRepository.save(project);
                return new ResponseEntity<Project>(project, HttpStatus.CREATED);
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "/rest/api/users/{uid}/projects/scdf", method = RequestMethod.POST)
    public ResponseEntity<Project> createProjectFromSCDF(@PathVariable Integer uid, @RequestBody List<TaskDefinition> tasks) {

        try {
            User user = usersRepository.findOne(uid);
            if (user == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                for (TaskDefinition taskDefinition : tasks){
                    if(taskDefinition.isComposed()){
                        Project project = projectsRepository.findByName(taskDefinition.getName() + "-SLA");
                        /*
                        Project does not exist in the database of the SLA Manager
                         */
                        if(project == null){
                            Project p = new Project();
                            p.setName(taskDefinition.getName() + "-SLA");
                            p.setType(ProjectType.MONITORING);
                            p.setPropertyCategoryCatalog("CSA");
                            p.setEventChannel(null);
                            p.setStatus(ProjectStatus.STARTED);
                            p.setUser(user);
                            projectsRepository.save(p);

                            CompositeService compositeService = new CompositeService();
                            compositeService.setName(taskDefinition.getName());
                            compositeService.setProject(p);
                            compositeServicesRepository.save(compositeService);

                            for(TaskDefinition task : tasks){
                                if(!task.isComposed() && task.getName().startsWith(taskDefinition.getName() + "-")){
                                    AtomicService atomicService = new AtomicService();
                                    atomicService.setName(task.getName());
                                    atomicService.setCompositeService(compositeService);

                                    atomicServicesRepository.save(atomicService);

                                    Asset asset = new Asset();
                                    asset.setName("service-operation-" + task.getName());
                                    asset.setType("OPERATION");
                                    asset.setProject(p);

                                    Asset a = assetsRepository.save(asset);

                                    String dsl = task.getDslText();

                                    StringTokenizer tokenizer = new StringTokenizer(dsl," ");

                                    String securityPropery = null;
                                    while (tokenizer.hasMoreTokens()){
                                        String token = tokenizer.nextToken();
                                        if(token.startsWith("--security-property=")){
                                            securityPropery = token.substring("--security-property=".length(), token.length());
                                        }
                                    }

                                    AssetSecurityPropertyPair assetSecurityPropertyPair = new AssetSecurityPropertyPair();
                                    SecurityProperty property = securityPropertiesRepository.findByName(securityPropery);

                                    AssetsSecuritypropertiesId id = new AssetsSecuritypropertiesId();
                                    id.setAid(a.getId());
                                    id.setSpid(property.getId());
                                    assetSecurityPropertyPair.setId(id);

                                    AssetSecurityPropertyPair pair = assetSecurityPropertyPairsRepository.save(assetSecurityPropertyPair);

                                    GuardedAction action = new GuardedAction();
                                    action.setAssetSecurityPropertyPair(pair);
                                    action.setAction("PENALTY");

                                    guardedActionsRepository.save(action);

                                    Operation operation = new Operation();
                                    operation.setAsset(a);
                                    operation.setAtomicService(atomicService);
                                    operation.setName(task.getName().replace(taskDefinition + "-",""));

                                    operationsRepository.save(operation);

                                }
                            }

                            this.translateProject(user.getId(),p.getId());
                        }
                    }
                }
                return new ResponseEntity<>(HttpStatus.CREATED);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/rest/api/users/{uid}/projects", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<List<Project>> getProjectsByUser(@PathVariable Integer uid) {

        try {
            User user = usersRepository.findById(uid);

            if (user != null) {
                List<Project> projects = projectsRepository.findByUserOrderByCreatedDesc(user);
                log.info("Fetching list of validation projects for user " + user.toString());
                return new ResponseEntity<List<Project>>(projects, HttpStatus.OK);
            } else {
                return new ResponseEntity<List<Project>>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<List<Project>>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "/rest/api/users/{uid}/projects/{pid}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<Project> getProjectById(@PathVariable Integer uid, @PathVariable Integer pid) {

        Project project = null;

        try {
            project = projectsRepository.findById(pid);

            if (project != null) {
                log.info("Fetching validation project " + project.toString());
                return new ResponseEntity<Project>(project, HttpStatus.OK);
            } else {
                log.info("Validation project with id:" + pid + " was not found");
                return new ResponseEntity<Project>(project, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "/rest/api/users/{uid}/projects/{pid}/negotiations", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<Negotiation> createNegotiation(@PathVariable Integer uid, @PathVariable Integer pid, @RequestBody Negotiation negotiation) {

        try {
            Project project = projectsRepository.findById(pid);
            User user = usersRepository.findById(negotiation.getUser().getId());
            negotiation.setCreated(new Timestamp(System.currentTimeMillis()));
            negotiation.setUser(user);
            negotiation.setProject(project);
            negotiation.setAction(NegotiationAction.STARTED);
            Negotiation neg = negotiationsRepository.save(negotiation);
            return new ResponseEntity<Negotiation>(neg, HttpStatus.CREATED);

        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "/rest/api/users/{uid}/assignedNegotiations", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<Set<AssignedNegotiation>> getAssignedNegotiations(@PathVariable Integer uid) {

        Set<AssignedNegotiation> assignedNegotiations = new HashSet<AssignedNegotiation>();

        try {
            User user = usersRepository.findById(uid);
            Set<Negotiation> negotiations = negotiationsRepository.findByUser(user.getId());

            for (Negotiation negotiation : negotiations) {
                Project p = new Project();
                p.setId(negotiation.getProject().getId());
                p.setUser(negotiation.getProject().getUser());
                p.setName(negotiation.getProject().getName());

                User u = new User();
                u.setId(negotiation.getUser().getId());
                u.setUsername(negotiation.getUser().getUsername());

                negotiation.setId(negotiation.getId());
                negotiation.setAction(negotiation.getAction());
                negotiation.setCreated(negotiation.getCreated());

                AssignedNegotiation assignedNegotiation = new AssignedNegotiation();
                assignedNegotiation.setNegotiation(negotiation);
                assignedNegotiation.setProject(p);
                assignedNegotiation.setUser(u);

                assignedNegotiations.add(assignedNegotiation);
            }

            return new ResponseEntity<Set<AssignedNegotiation>>(assignedNegotiations, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "/rest/api/users/{uid}/guaranteeTermsForAssignedNegotiations", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<Set<GuaranteeTerm>> getGuaranteeTermsForAssignedNegotiations(@PathVariable Integer uid) {

        Set<GuaranteeTerm> guaranteeTerms = new HashSet<GuaranteeTerm>();

        try {
            User user = usersRepository.findById(uid);
            Set<Negotiation> negotiations = negotiationsRepository.findByUser(user.getId());
            for (Negotiation negotiation : negotiations) {
                Project project = negotiation.getProject();
                for (Asset asset : project.getAssets()) {
                    Set<AssetSecurityPropertyPair> pairs = asset.getAssetSecuritypropertyPairs();
                    for (AssetSecurityPropertyPair pair : pairs) {
                        guaranteeTerms.addAll(pair.getGuaranteeTerms());
                    }
                }
            }
            return new ResponseEntity<Set<GuaranteeTerm>>(guaranteeTerms, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "/rest/api/users/{uid}/projects/{pid}/negotiations/{nid}", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<NegotiationRequest> createNegotiationRequest(@PathVariable Integer uid, @PathVariable Integer pid, @PathVariable Integer nid, @RequestBody NegotiationRequest request) {

        try {
            Negotiation negotiation = negotiationsRepository.findOne(nid);
            request.setNegotiation(negotiation);
            request.setCreated(new Timestamp(System.currentTimeMillis()));
            NegotiationRequest negotiationRequest = negotiationRequestRepository.save(request);
            return new ResponseEntity<NegotiationRequest>(negotiationRequest, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "/rest/api/users/{uid}/projects/{pid}/negotiations/{nid}", method = RequestMethod.PUT, consumes = "application/json")
    public ResponseEntity<Negotiation> updateNegotiationRequest(@PathVariable Integer uid, @PathVariable Integer pid, @PathVariable Integer nid, @RequestBody Negotiation negotiation) {

        try {
            Negotiation neg = negotiationsRepository.findOne(nid);
            neg.setAction(negotiation.getAction());
            negotiationsRepository.save(neg);
            return new ResponseEntity<Negotiation>(neg, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "/rest/api/explorations", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<Exploration> createExploration(@RequestBody Exploration exploration) {

        try {
            exploration.setCreated(new Timestamp(System.currentTimeMillis()));
            explorationsRepository.save(exploration);
            return new ResponseEntity<Exploration>(exploration, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/rest/api/users/{uid}/projects/{pid}/translate", method = RequestMethod.POST)
    @Transactional
    public ResponseEntity<Project> translateProject(@PathVariable Integer uid, @PathVariable Integer pid) {
        try {
            Project project = projectsRepository.findOne(pid);

            /*
             * Create the WS Agreement from the assets and guarded actions
             */
            String xml = "";

            List<Asset> assets = project.getAssets();

            // TODO Maria you should the code below this section
            Integer i = 1;

            xml += ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<wsag:AgreementOffer xmlns:wsa=\"http://www.w3.org/2005/08/addressing\" "
                    + "xmlns:asrt=\"http://www.cumulus.org/certificate/model\" "
                    + "xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\" "
                    + "xmlns:wsag=\"http://schemas.ggf.org/graap/2007/03/ws-agreement\" "
                    + "xmlns:wsrf-bf=\"http://docs.oasis-open.org/wsrf/bf-2\" "
                    + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                    + "xmlns:hfp=\"http://www.w3.org/2001/XMLSchema-hasFacetAndProperty\" "
                    + "xsi:schemaLocation=\"http://schemas.ggf.org/graap/2007/03/ws-agreement file:ws-agreement-demo.xsd\"> "
                    + "<wsag:Context> " + "        <wsag:ServiceProvider>AgreementInitiator</wsag:ServiceProvider> " +
                    /*
                     * "        <wsag:SLA_LC_Parameters> " + "            <asrt:IntParam> " +
                     * "                <VariableName>TooManyVio</VariableName> " +
                     * "                <InitialValue> " +
                     * "                    <asrt:Int value=\"1\"/> " +
                     * "                </InitialValue> " + "            </asrt:IntParam> " +
                     * "            <asrt:DurationParam> " +
                     * "                <VariableName>expiryTime</VariableName> " +
                     * "                <InitialValue> " + "                    <asrt:Days> " +
                     * "                        <asrt:Int value=\"10\"/> " +
                     * "                    </asrt:Days> " + "                </InitialValue> " +
                     * "            </asrt:DurationParam> " + "        </wsag:SLA_LC_Parameters> " +
                     */
                    "    </wsag:Context> " + "    <wsag:Terms> " + "        <wsag:All>");

            for (Asset asset : assets) {
                for (AssetSecurityPropertyPair p : asset.getAssetSecuritypropertyPairs()) {
                    if (p != null) {
                        String SDTID = "SDT" + (i++); // <-- fixed values
                        String assetName = asset.getName();
                        String assetType = asset.getType();

                        if (assetType.equals("operation")) {
                            String output = asset.getOperation().getOutputmessage();
                            String input = asset.getOperation().getInputmessage();

                            xml += ("<wsag:ServiceDescriptionTerm wsag:Name=\"" + SDTID + "\" wsag:ServiceName=\""
                                    + assetName + "\"> " + "    <wsag:Type>InternalOperation</wsag:Type> "
                                    + "    <any xmlns=\"http://www.w3.org/2001/XMLSchema\">  " + "        <annotation> "
                                    + "            <documentation> " + "                <wsdl:Definition name=\" "
                                    + assetName + "\"> " + "<message name=\"" + input + "\"> ");

                            for (Input IN : asset.getOperation().getInputs()) {
                                String inputParameterName = IN.getName();
                                String inputParameterType = IN.getType();

                                xml += ("<part name=\"" + inputParameterName + "\" type=\"" + inputParameterType
                                        + "/> ");

                            }

                            xml += ("            </message> " + "                    <message name=\"" + output
                                    + "\"> ");

                            for (uk.ac.city.toreador.rest.api.toreador.entities.Output OUT : asset.getOperation().getOutputs()) {
                                // for (String InputParameters : Inputs) {
                                String outputParameterName = OUT.getName();
                                String outputParameterType = OUT.getType();

                                xml += ("<part name=\"" + outputParameterName + "\" type=\"" + outputParameterType
                                        + "/> ");
                            }

                            xml += (" </message> " + "                    <portType name=\"" + assetName
                                    + "_PortType\"> " + "                        <operation name=\"" + assetName
                                    + "Result\"> " + "                            <input message=\"" + input + "\"/> "
                                    + "                            <output message=\"" + output + "\"/> "
                                    + "                        </operation> " + "                    </portType> "
                                    + "                    <binding> ... </binding> "
                                    + "                    <service name=\"" + assetName + "\"> "
                                    + "                        <port binding=\"tns:" + assetName + "_Binding\" "
                                    + "                            name=\"" + assetName + "_Port\"> </port> "
                                    + "                    </service> " + "                </wsdl:Definition> "
                                    + "            </documentation> " + "        </annotation> " + "    </any> "
                                    + "</wsag:ServiceDescriptionTerm>");
                        } else if (assetType.equals("input") | assetType.equals("output")) {
                            String ParamType = "";

                            xml += (" <wsag:ServiceDescriptionTerm wsag:Name=\"" + SDTID + "\" wsag:ServiceName=\""
                                    + assetName + "\"> " + "   <wsag:Type>DataModel</wsag:Type> "
                                    + "   <any xmlns=\"http://www.w3.org/2001/XMLSchema\">  " + "       <annotation> "
                                    + "           <documentation> " + "               <wsdl:types> "
                                    + "                   <ComplexType name=\"" + assetName + "\"> "
                                    + "                       <sequence> "
                                    + "                           <element name=\"" + assetName + "\" type=\"" +
                                    // if type is input get the type of the
                                    // input parameter
                                    (assetType.equals("input") /* ) != null */ /* XXX */
                                            ? asset.getInput().getType()
                                            // else, if type is output get the type of the
                                            // output parameter
                                            : asset.getOutput().getType())
                                    + "\"/> " + "                       </sequence> "
                                    + "                   </ComplexType> " + "                </wsdl:types>  "
                                    + "            </documentation> " + "        </annotation> " + "    </any> "
                                    + "</wsag:ServiceDescriptionTerm>");
                        }

                        String GTname = asset.getName() + "_" + p.getSecurityProperty().getName();

                        String SecurityProperty = p.getSecurityProperty().getName();
                        String Asset = asset.getName();
                        String rate = p.getRate();

                        xml += ("<wsag:GuaranteeTerm wsag:Name=\"" + GTname + "\" wsag:Obligated=\"ServiceProvider\">"
                                + "             <wsag:ServiceLevelObjective> "
                                + "                 <wsag:CustomServiceLevel> "
                                + "                     <wsag:DeclarativeLevel> "
                                + "                         <wsag:SLO_Category>" + SecurityProperty
                                + "</wsag:SLO_Category> "
                                + "                         <wsag:ServiceAsset>SDT1/wsdl:definition/portType/" + "/"
                                + Asset + "</wsag:ServiceAsset>" + "                     </wsag:DeclarativeLevel> "
                                + "                     <wsag:ProceduralLevel> "
                                + "                         <wsag:SLOTemplate wsag:Name=\"" + SecurityProperty + "\"> "
                                + "                             <wsag:SLOTemplateParameters> " /*
                         * <-- Not needed for
                         * validation project.
                         */
                                /* <-- Needed for monitoring project. */
                                + "                                 <wsag:SLOTemplateParameter name=\"" + "\" "
                                + "                                     value=\"" + "\"/>  "
                                + "                                 <wsag:SLOTemplateParameter name=\"Metric\" value=\""
                                + "\"/>  " + "                             </wsag:SLOTemplateParameters> "
                                + "                         </wsag:SLOTemplate> "
                                + "                         <wsag:Assertion ID=\"" + GTname + "\">  "
                                + "                             <InterfaceDeclr> "
                                + "                                 <ID></ID> "
                                + "                                 <ProviderRef></ProviderRef> "
                                + "                                 <Interface> "
                                + "                                     <InterfaceRef> "
                                + "                                         <InterfaceLocation></InterfaceLocation> "
                                + "                                     </InterfaceRef> "
                                + "                                 </Interface> "
                                + "                             </InterfaceDeclr> "
                                + "                             <VariableDeclr> "
                                + "                                 <varName></varName> "
                                + "                                 <varType></varType> "
                                + "                             </VariableDeclr> "
                                + "                             <Guaranteed ID=\"\" type=\"\"> "
                                + "                                 <quantification> "
                                + "                                     <quantifier>forall</quantifier> "
                                + "                                     <timeVariable> "
                                + "                                         <varName></varName> " /*
                         * XXX - what's this
                         * nameless
                         * variable?????
                         */
                                + "                                         <varType></varType> "
                                + "                                     </timeVariable> "
                                + "                                 </quantification> "
                                + "                                 <postcondition> "
                                + "                                     <atomicCondition> "
                                + "                                         <eventCondition> "
                                + "                                             <event> "
                                + "                                                 <eventID> "
                                + "                                                     <varName></varName> " /*
                         * XXX -
                         * what'
                         * s
                         * this
                         * nameless
                         * variable
                         * ?????
                         */
                                + "                                                 </eventID> "
                                + "                                             </event> "
                                + "                                         </eventCondition> "
                                + "                                     </atomicCondition> "
                                + "                                 </postcondition> "
                                + "                             </Guaranteed> "
                                + "                         </wsag:Assertion> "
                                + "                     </wsag:ProceduralLevel> "
                                + "                 </wsag:CustomServiceLevel> "
                                + "             </wsag:ServiceLevelObjective> "
                                + "             <wsag:BusinessValueList> "
                                + "                 <wsag:CustomBusinessValue> ");

                        for (GuardedAction gaction : p.getGuardedActions()) {
                            String guard = gaction.getGuard();
                            Integer PenaltyValue = gaction.getPenalty();
                            xml += ("              <wsag:GuardedAction> " + ((gaction.getAction().equals("RENEGOTIATE"))
                                    ? "<wsag:ReNegotiate/>"
                                    : ((gaction.getAction().equals("PENALTY"))
                                    ? ("<wsag:Penalty> " + "<wsag:Value>" + PenaltyValue + "</wsag:Value>"
                                    + "<wsag:ValueUnit>GBP</wsag:ValueUnit>" + "</wsag:Penalty>")
                                    : ("                     <wsag:Other> "
                                    + "                         <wsag:ActionName>" + gaction.getName()
                                    + "</wsag:ActionName> " + "                     </wsag:Other> ")))
                                    + "                         <wsag:ValueExpr> " + guard
                                    + "                         </wsag:ValueExpr> "
                                    + "                     </wsag:GuardedAction> ");
                        }

                        xml += ("                     <wsag:Rate> <asrt:" + p.getTimeunit() + ">" + rate + "</asrt:"
                                + p.getTimeunit() + "></wsag:Rate> " + "                 </wsag:CustomBusinessValue> "
                                + "             </wsag:BusinessValueList> " + "         </wsag:GuaranteeTerm>");
                    }
                }
            }

            xml += ("        </wsag:All> " + "</wsag:Terms> " + "</wsag:AgreementOffer>");

            project.setWsagreement(xml.getBytes());
            project.setStatus(ProjectStatus.CREATED);
            projectsRepository.save(project);

            /*
             * Translate the WS Agreement, create the prism model and store it into the
             * database
             */
            InputStream file = IOUtils.toInputStream(xml);
            Translate tr = new Translate();

            String[] results = tr.translateSLAtoPrismAndLisp(file, Translate.TranslationMode.NEGOTIATION_COST);
            // log.info(results[0]); // The Prism model
            // log.info(results[1]); // The Lisp code

            byte[] model = results[0].getBytes();

            project.setModel(model);
            projectsRepository.save(project);

            return new ResponseEntity<Project>(HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();

            // log.error(e.pr);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "/rest/api/users/{uid}/projects/{pid}", method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
    public ResponseEntity<Project> updateProject(@PathVariable Integer uid, @PathVariable Integer pid,
                                                 @RequestBody Project project) {

        try {

            Project p = projectsRepository.findById(pid);

            p.setName(project.getName());
            p.setPropertyCategoryCatalog(project.getPropertyCategoryCatalog());
            p.setStatus(project.getStatus());

            projectsRepository.save(p);
            log.info("Validation project updated successully " + p);
            return new ResponseEntity<Project>(p, HttpStatus.CREATED);

        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "/rest/api/users/{uid}/projects/{pid}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteProjectById(@PathVariable Integer uid, @PathVariable Integer pid) {

        try {
            projectsRepository.delete(pid);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "/rest/api/users/{uid}/projects/{pid}/{file}", method = RequestMethod.GET)
    public ResponseEntity<String> getFileFromProjectById(@PathVariable Integer uid, @PathVariable Integer pid,
                                                         @PathVariable String file) {

        try {

            Project p = projectsRepository.findById(pid);
            byte[] f = null;

            final HttpHeaders headers = new HttpHeaders();

            switch (file) {
                case "properties":
                    f = p.getProperties();
                    headers.setContentType(MediaType.TEXT_PLAIN);
                    break;
                case "model":
                    f = p.getModel();
                    headers.setContentType(MediaType.TEXT_PLAIN);
                    break;
                case "validationoutput":
                    f = p.getValidationoutput();
                    headers.setContentType(MediaType.TEXT_PLAIN);
                    break;
                case "wsagreement":
                    f = p.getWsagreement();
                    headers.setContentType(MediaType.APPLICATION_XML);
                    break;
                default:
                    f = null;
                    break;
            }
            if (f != null) {
                return new ResponseEntity<String>(new String(f, "UTF-8"), headers, HttpStatus.OK);
            } else {
                log.info(file + " for project with id " + pid + " does not exist in the database");
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "/rest/api/users/{uid}/projects/{pid}/compositeservices/{cid}/owls", method = RequestMethod.GET)
    public ResponseEntity<String> getOwlsFromCompositeService(@PathVariable Integer uid, @PathVariable Integer pid,
                                                              @PathVariable Integer cid) {

        try {

            CompositeService cservice = compositeServicesRepository.findOne(cid);

            if (cservice != null) {

                byte[] owls = cservice.getOwls();

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_XML);

                log.info("OWL-S for composite service with id " + cservice.getId() + " has been retrieved successfully");
                return new ResponseEntity<String>(new String(owls, "UTF-8"), headers, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "/rest/api/users/{uid}/projects/{pid}/compositeservices/{cid}/atomicservices/{aid}", method = RequestMethod.GET)
    public ResponseEntity<String> getOwlFromAtomicService(@PathVariable Integer uid, @PathVariable Integer pid,
                                                          @PathVariable Integer cid, @PathVariable Integer aid) {

        try {

            AtomicService aservice = atomicServicesRepository.findOne(aid);

            if (aservice != null) {

                byte[] owl = aservice.getOwl();

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_XML);
                log.info("OWL for atomic service with id " + aservice.getId() + " has been retreived successully");
                return new ResponseEntity<String>(new String(owl, "UTF-8"), headers, HttpStatus.OK);
            } else {
                return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "/rest/api/users/{uid}/projects/{pid}/assetsecuritypropertypair/{aid},{spid}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteAssetSecurityPropertyPairById(@PathVariable Integer uid, @PathVariable Integer pid,
                                                                 @PathVariable Integer aid, @PathVariable Integer spid) {

        try {
            assetSecurityPropertyPairsRepository.delete(new AssetsSecuritypropertiesId(aid, spid));
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "/rest/api/users/{uid}/projects/{pid}/assetsecuritypropertypair/{aid},{spid}/guardedactions/{gid}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteGuardedActionById(@PathVariable Integer uid, @PathVariable Integer pid,
                                                     @PathVariable Integer aid, @PathVariable Integer spid, @PathVariable Integer gid) {

        try {
            guardedActionsRepository.delete(gid);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "/rest/api/users/{uid}/projects/{pid}/servicemodelitem", method = RequestMethod.POST, headers = "content-type=multipart/*")
    public ResponseEntity<String> uploadServiceModelItem(@PathVariable Integer uid, @PathVariable Integer pid,
                                                         @RequestParam("file") MultipartFile file) {

        try {

            Project project = projectsRepository.findOne(pid);
            if (project != null) {

                File tempdir = Files.createTempDir();
                InputStream input = file.getInputStream();
                File tmpfile = new File(tempdir.getAbsolutePath() + "/" + file.getOriginalFilename());
                OutputStream out = new FileOutputStream(tmpfile);
                IOUtils.copy(input, out);
                input.close();
                out.close();

                return new ResponseEntity<String>(tmpfile.getAbsolutePath(), HttpStatus.CREATED);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "/rest/api/users/{uid}/projects/{pid}/servicemodel", method = RequestMethod.POST)
    public ResponseEntity<Project> uploadServiceModel(@PathVariable Integer uid, @PathVariable Integer pid,
                                                      @RequestBody String[] paths) {

        try {

            XQDataSource ds = new com.saxonica.xqj.SaxonXQDataSource();
            XQConnection con = ds.getConnection();

            ClassPathResource resource = new ClassPathResource("Assets.xq");
            InputStream query = resource.getInputStream();

            XQStaticContext ctx = con.getStaticContext();
            ctx.setBaseURI(query.toString());
            XQPreparedExpression expr = con.prepareExpression(query, ctx);

            File tempdir = Files.createTempDir();

            Project project = projectsRepository.findOne(pid);

            File[] files = new File[paths.length];
            int counter = 0;

            for (String path : paths) {
                log.info("Reading file with filename:" + path);
                File f = new File(path.trim().replaceAll("\"", ""));
                InputStream in = new FileInputStream(f);
                OutputStream out = new FileOutputStream(tempdir.getAbsolutePath() + File.separatorChar + f.getName());
                IOUtils.copy(in, out);
                files[counter] = f;
                counter++;
            }

            expr.bindObject(new QName("theinput"), new String(files[0].getName()), null);
            expr.bindObject(new QName("thedirectory"), tempdir.getAbsolutePath(), null);
            query.close();

            String json = expr.executeQuery().getSequenceAsString(null).replace("\" ", "\"").replace(" \"", "\"")
                    .replace(", ]", " ]");

            JSONParser parser = new JSONParser();
            Object obj = parser.parse(json);

            JSONObject jsonObject = (JSONObject) obj;
            JSONObject compositeServiceObj = (JSONObject) jsonObject.get("compositeService");

            CompositeService compositeService = new CompositeService();
            compositeService.setName((String) compositeServiceObj.get("name"));
            compositeService.setProject(project);

            compositeService.setOwls(IOUtils.toByteArray(new FileInputStream(
                    new File(tempdir.getAbsolutePath() + File.separatorChar + files[0].getName()))));

            compositeServicesRepository.save(compositeService);

            JSONArray atomicServices = (JSONArray) compositeServiceObj.get("atomicServices");
            Iterator<?> atomicServicesiterator = atomicServices.iterator();

            while (atomicServicesiterator.hasNext()) {

                JSONObject atomicServiceObj = (JSONObject) atomicServicesiterator.next();

                AtomicService atomicService = new AtomicService();
                atomicService.setName((String) atomicServiceObj.get("serviceName"));
                atomicService.setCompositeService(compositeService);
                atomicService.setOwl(
                        IOUtils.toByteArray(new FileInputStream(new File(tempdir.getAbsolutePath() + File.separatorChar
                                + ((String) atomicServiceObj.get("serviceName")) + ".owl"))));
                /*
                 * Store the newly created atomic service in the atomicService variable to use
                 * it later on
                 */
                AtomicService tempAtomicservice = atomicServicesRepository.save(atomicService);

                JSONArray operations = (JSONArray) atomicServiceObj.get("operations");
                Iterator<?> operationsIterator = operations.iterator();

                while (operationsIterator.hasNext()) {
                    JSONObject operationObj = (JSONObject) operationsIterator.next();

                    /*
                     * Store the operation as an asset of type operation
                     */
                    Asset operationAsset = new Asset();
                    operationAsset.setProject(project);
                    operationAsset.setName("service_" + compositeService.getName() + "_operation_"
                            + operationObj.get("operationName"));
                    operationAsset.setType("OPERATION");
                    Asset tempOperationAsset = assetsRepository.save(operationAsset);

                    /*
                     * Create and store the newly created operation @ table operations
                     */
                    Operation operation = new Operation();
                    operation.setName((String) operationObj.get("operationName"));
                    operation.setInputmessage((String) operationObj.get("inputMessageName"));
                    operation.setOutputmessage((String) operationObj.get("outputMessageName"));
                    operation.setAtomicService(tempAtomicservice);
                    operation.setAsset(tempOperationAsset);
                    Operation tempOperation = operationsRepository.save(operation);

                    /*
                     * Store an asset of type output
                     */
                    if(((String)operationObj.get("outputName")).length() > 0){
                        Asset outputAsset = new Asset();
                        outputAsset.setProject(project);
                        outputAsset.setName("service_" + compositeService.getName() + "_operation_" + operation.getName()
                                + "_output_" + (String) operationObj.get("outputName"));
                        outputAsset.setType("OUTPUT");
                        Asset tempOutputAsset = assetsRepository.save(outputAsset);

                        /*
                         * Create and store the output of the operation @ table outputs
                         */

                        Output output = new Output();
                        output.setOperation(tempOperation);
                        output.setName((String) operationObj.get("outputName"));
                        output.setType((String) operationObj.get("outputType"));
                        output.setAsset(tempOutputAsset);
                        outputsRepository.save(output);
                    }


                    JSONArray inputsObj = (JSONArray) operationObj.get("inputParameters");
                    Iterator<?> inputsIterator = inputsObj.iterator();

                    while (inputsIterator.hasNext()) {
                        JSONObject inputObj = (JSONObject) inputsIterator.next();

                        /*
                         * Store the operation as an asset of type input
                         */
                        Asset inputAsset = new Asset();
                        inputAsset.setProject(project);
                        inputAsset.setName("service_" + compositeService.getName() + "_operation_" + operation.getName()
                                + "_input_" + (String) inputObj.get("inputName"));
                        inputAsset.setType("INPUT");
                        Asset tempInputAsset = assetsRepository.save(inputAsset);

                        Input input = new Input();
                        input.setName((String) inputObj.get("inputName"));
                        input.setOperation(tempOperation);
                        input.setType((String) inputObj.get("inputType"));
                        input.setAsset(tempInputAsset);
                        inputsRepository.save(input);
                    }
                }
            }

            return new ResponseEntity<Project>(HttpStatus.OK);

        } catch (FileNotFoundException e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error(e.getCause());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "/rest/api/projects/validation/{pid}/uploadtest", method = RequestMethod.POST, headers = "content-type=multipart/*")
    public ResponseEntity<?> uploadTestFile(@PathVariable Long pid, @RequestParam("files") MultipartFile[] files) {

        for (MultipartFile file : files) {
            log.info(file.getOriginalFilename() + " - " + file.getName() + " - " + file.getSize());
        }

        return new ResponseEntity<Project>(HttpStatus.CREATED);
    }

    @Transactional
    @RequestMapping(value = "/rest/api/users/{uid}/projects/{pid}/assetsecuritypropertypair", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<?> createAssetSecurityPropertyPair(
            @RequestBody AssetSecurityPropertyPair assetSecurityPropertyPair) {

        try {
            assetSecurityPropertyPairsRepository.save(assetSecurityPropertyPair);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "/rest/api/users/{uid}/projects/{pid}/guardedactions", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<?> createGuardedAction(@RequestBody GuardedAction guardedAction) {

        try {
            guardedActionsRepository.save(guardedAction);

            GuaranteeTerm gt = new GuaranteeTerm();
            gt.setAssetSecurityPropertyPair(guardedAction.getAssetSecurityPropertyPair());
            gt.setName(guardedAction.getName());
            guarateeTermsRepository.save(gt);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "/rest/api/users/{uid}/projects/{pid}/guaranteeTerms", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<?> updateGuaranteeTerm(@RequestBody GuaranteeTerm term) {

        try {
            guarateeTermsRepository.save(term);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "/rest/api/users/{uid}/projects/{pid}/properties", method = RequestMethod.POST, headers = "content-type=multipart/*")
    public ResponseEntity<?> uploadPropertiesFile(@PathVariable Integer uid, @PathVariable Integer pid,
                                                  @RequestParam("file") MultipartFile file) {

        try {

            Project project = projectsRepository.findOne(pid);
            if (project != null) {
                project.setProperties(file.getBytes());
                projectsRepository.save(project);
                return new ResponseEntity<Project>(HttpStatus.CREATED);
            } else {
                return new ResponseEntity<Project>(HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "/rest/api/securityProperties", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<List<SecurityProperty>> getSecurityProperties() {

        try {
            return new ResponseEntity(securityPropertiesRepository.findAllByOrderByIdAsc(), HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "/rest/api/users/{uid}/projects/{pid}/slos", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<?> createSLO(@PathVariable Integer uid, @PathVariable Integer pid, @RequestBody SloRequest sloRequest) {

        try {
            VelocityContext context = new VelocityContext();
            File fileTemplate = File.createTempFile("toreador-slo-template-", ".vm");
            String xmlTemplate = sloTemplatesRepository.findOne(sloRequest.getSloTemplate().getId()).getXml();
            Set<SloParameter> sloParameters = sloTemplatesRepository.findOne(sloRequest.getSloTemplate().getId()).getSloParameters();
            FileUtils.writeStringToFile(fileTemplate, xmlTemplate, "UTF-8");
            engine.setProperty("file.resource.loader.path", "");
            Template template = engine.getTemplate(fileTemplate.getCanonicalPath());

            Map<String, Object> parameters = sloRequest.getParameters();
            if (parameters != null) {
                for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    ParameterType type = null;
                    for (SloParameter slop : sloParameters) {
                        if (slop.getName().equalsIgnoreCase(key))
                            type = slop.getType();
                    }

                    switch (type) {
                        case STRING:
                            if (NumberUtils.isCreatable(String.valueOf(value))) {
                                context.put(key, NumberUtils.createNumber(String.valueOf(value)));
                            } else {
                                context.put(key, String.valueOf(value));
                            }
                            break;
                        case LIST:
                            context.put(key, String.valueOf(value).split(","));
                            break;
                        case ENUMERATION:
                            if (NumberUtils.isCreatable(String.valueOf(value))) {
                                context.put(key, NumberUtils.createNumber(String.valueOf(value)));
                            } else {
                                context.put(key, String.valueOf(value));
                            }
                            break;
                    }
                }
            }

            StringWriter ecassertion = new StringWriter();
            template.merge(context, ecassertion);

            Project project = projectsRepository.findById(pid);
            Asset asset = assetsRepository.findOne(sloRequest.getAsset().getId());
            SecurityProperty securityProperty = securityPropertiesRepository.findOne(sloRequest.getSecurityProperty().getId());

            Slo slo = new Slo();
            slo.setProject(project);
            slo.setAsset(asset);
            slo.setProperty(securityProperty);
            slo.setEcassertion(ecassertion.toString());

            slosRepository.save(slo);

            final File sloXml = File.createTempFile("toreador-slo-ecassertion-", ".xml");
            FileUtils.writeStringToFile(sloXml, ecassertion.toString());

            project.setStatus(ProjectStatus.START_MONITOR);
            projectsRepository.save(project);

           Thread.sleep(6000);

            final ProcessBuilder toreadorClientAdd = new ProcessBuilder("java", "-jar", "/home/abfc149/ToreadorSLAClient2.jar", "add", sloXml.getCanonicalPath());

            Object[] cmd = (Object[]) toreadorClientAdd.command().toArray();

            StringBuffer buf = new StringBuffer();

            for (Object item : cmd) {
                buf.append(item.toString() + " ");
            }
            log.info(String.format("Running command %s ...", buf.toString()));

            Process process = toreadorClientAdd.start();
            int result = process.waitFor();

            if (result == 0) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder builder = new StringBuilder();
                String line = null;
                String cid = null;
                int count = 0;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                    builder.append(System.getProperty("line.separator"));
                    if (count == 3) {
                        Pattern pattern = Pattern.compile("cumulus:cm:id:monitoring:[0-9]+");
                        Matcher matcher = pattern.matcher(line);
                        if (matcher.find())
                            cid = matcher.group(0);
                    }
                    count++;
                }

                log.info(String.format("Added cumulus certificate with id %s successfully", cid));

                final ProcessBuilder toreadorClientSubmit = new ProcessBuilder("java", "-jar", "/home/abfc149/ToreadorSLAClient2.jar", "submit", cid);

                Process p = toreadorClientSubmit.start();
                cmd = (Object[]) toreadorClientSubmit.command().toArray();
                buf = new StringBuffer();

                for (Object item : cmd) {
                    buf.append(item.toString() + " ");
                }
                log.info(String.format("Running command %s ...", buf.toString()));

                int submit = p.waitFor();
                if (submit == 0) {
                    log.info(String.format("Submitted cumulus certificate with id %s successfully", cid));
                }
            }

            if(sloRequest.isLast()){
                project.setType(ProjectType.MONITORING);
                project.setStatus(ProjectStatus.STARTED);
                projectsRepository.save(project);
            }

            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "/rest/api/everest/templates", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<List<uk.ac.city.toreador.rest.api.everest.entities.Template>> getAllTemplates() {

        try {
            return new ResponseEntity(templateRepository.findAll(), HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "/rest/api/users/{uid}/projects/{pid}/slos/{sloId}/ecassertion", method = RequestMethod.GET, produces = MediaType.TEXT_XML_VALUE)
    public byte[] getECAssertionForSlo(
            @PathVariable Integer uid,
            @PathVariable Integer pid,
            @PathVariable Integer sloId) {

        try {
            String ecassertion = slosRepository.findOne(sloId).getEcassertion();
            InputStream inputStream = new ByteArrayInputStream(ecassertion.getBytes(Charset.forName("UTF-8")));
            return IOUtils.toByteArray(inputStream);
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    @Transactional
    @RequestMapping(value = "/rest/api/slotemplates/{slotemplateId}/info", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public byte[] getDescriptionForSecurityProperty(@PathVariable Integer slotemplateId) {

        try {
            Slotemplate slotemplate = sloTemplatesRepository.findOne(slotemplateId);
            InputStream inputStream = new ByteArrayInputStream(slotemplate.getDescription().getBytes(Charset.forName("UTF-8")));
            return IOUtils.toByteArray(inputStream);
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    @Transactional
    @RequestMapping(value = "/rest/api/slotemplates/{slotemplateId}/xml", method = RequestMethod.GET, produces = MediaType.TEXT_XML_VALUE)
    public byte[] getXMLTemplateForSecurityProperty(@PathVariable Integer slotemplateId) {

        try {
            Slotemplate slotemplate = sloTemplatesRepository.findOne(slotemplateId);
            InputStream inputStream = new ByteArrayInputStream(slotemplate.getXml().getBytes(Charset.forName("UTF-8")));
            return IOUtils.toByteArray(inputStream);
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    @Transactional
    @RequestMapping(value = "/rest/api/users/{uid}/projects/{pid}/slos/{sloId}/templatesDefined", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<List<TemplateDefined>> getTemplatesDefined(
            @PathVariable Integer uid,
            @PathVariable Integer pid,
            @PathVariable Integer sloId) {

        try {
            Slo slo = slosRepository.findOne(sloId);
            if(slo != null){
                List<TemplateDefined> templatesDefined = templateDefinedRepository.findByTemplateIdContaining(slo.getProperty().getName().toUpperCase());
                return new ResponseEntity(templatesDefined, HttpStatus.OK);
            }else {
                return new ResponseEntity(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "/rest/api/users/{uid}/projects/{pid}/slos/{sloId}/templatesDefined/{tempDefId}/events", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<List<Event>> getEventForTemplateDefined(@PathVariable Integer tempDefId) {

        List<Event> events = new ArrayList<Event>();

        String bodyEventId = null;
        String headPredicate = null;
        Event bodyEvent = null;

        TemplateDefined templateDefined = templateDefinedRepository.findOne(tempDefId);
        String templateString = templateDefined.getTemplateString();

        if(templateDefined != null){
            Pattern bodyPattern = Pattern.compile("body=\\[(?<predicate>[a-zA-Z0-9]+)\\(([a-zA-Z0-9]+=[a-zA-Z0-9()]+,*)*eventId=(?<eventId>[a-zA-Z0-9()]+),");
            Matcher bodyMatcher = bodyPattern.matcher(templateString);
            if (bodyMatcher.find())
                bodyEventId = bodyMatcher.group("eventId");

                bodyEvent = getEventForEventId(bodyEventId);

                if(bodyEvent != null)
                    events.add(bodyEvent);
            }

            Pattern headPattern = Pattern.compile("head=\\[(?<predicate>[a-zA-Z0-9]+)\\(([a-zA-Z0-9]+=[a-zA-Z0-9()]*,*)+eventId=(?<eventId>[a-zA-Z0-9]*),*(fluent=(?<fluent>[a-zA-Z0-9]*))*");
            Matcher headMatcher = headPattern.matcher(templateString);
            String headEventId = null;
            String fluent = null;
            if (headMatcher.find()){
                headPredicate = headMatcher.group("predicate");
                headEventId =  headMatcher.group("eventId");
                fluent = headMatcher.group("fluent");
            }

            if(headPredicate != null){
                if("Happens".equalsIgnoreCase(headPredicate)){
                    Event event = getEventForEventId(headEventId);
                    if(event != null)
                        events.add(event);
                }else if("HoldsAt".equalsIgnoreCase(headPredicate)){

                    List<EventParameter> parameters = bodyEvent.getParameters();
                    Iterator<EventParameter> iterator = parameters.iterator();
                    StringBuffer buffer = new StringBuffer();
                    int i= 0;
                    Event fromEvent = null;
                    if(fluent.contains("write")){
                        while(iterator.hasNext()){
                            EventParameter params = iterator.next();
                            buffer.append("fVar" + i++ + "=\"" + params.getValue() + "\" and ");
                        }

                        String sql = "SELECT fromEvent FROM everestfluentdb.fl" + fluent + " where name=\"" + fluent + "\" and " + buffer.toString().substring(0, buffer.toString().length() - 5);

                        fromEvent = eventRepository.findByEventId(template.queryForObject(sql, String.class));
                        events.add(getEventForEventId(fromEvent.getEventId()));
                    }
                }
            }

        return new ResponseEntity(events, HttpStatus.OK);
    }

    private Event getEventForEventId(String eventId){

        if(eventId != null){
            Event event = eventRepository.findByEventId(eventId);
            String eventString = event.getEventString();

            Pattern headVariablesPattern = Pattern.compile("uk.ac.city.soi.everest.core.Variable\\{name=(?<name>[a-zA-Z0-9.]+),type=(?<type>[a-zA-Z]+),value=(?<value>[a-zA-Z0-9-@.]+)\\}");
            Matcher headVariablesMatcher = headVariablesPattern.matcher(eventString);

            while (headVariablesMatcher.find()) {

                String name = headVariablesMatcher.group("name").substring(1);
                String value = headVariablesMatcher.group("value");

                if(!name.equalsIgnoreCase("status")
                        && !name.equalsIgnoreCase("sender")
                        && !name.equalsIgnoreCase("receiver")
                        && !name.equalsIgnoreCase("source")
                        && !name.equalsIgnoreCase("serviceId")){

                    EventParameter parameter = new EventParameter();
                    parameter.setName(name);
                    parameter.setValue(value);
                    event.getParameters().add(parameter);
                }
            }
            EventParameter parameter = new EventParameter();
            parameter.setName("timestamp");
            parameter.setValue(event.getTimestamp().toString());

            return event;

        }

        return null;
    }

}