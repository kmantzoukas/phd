package uk.ac.city.toreador.validation;

import org.cumulus.certificate.model.*;
import org.ggf.schemas.graap._2007._03.ws_agreement.*;
import org.ggf.schemas.graap._2007._03.ws_agreement.Other;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Translator

public class Translate {
    public enum TranslationMode {
        VALIDATION,
        NEGOTIATION_TIME,
        NEGOTIATION_COST
    }

    protected TranslationMode translationMode
            = TranslationMode.VALIDATION;

    protected EvalNode timeUnit = null;
    // functionsN - N is the number of parameters
    private final static String functions0[] = {// "DetectedAnomalies"
            // , "DetectedConflicts"
            // , "ResolvedAnomalies"
            // , "ResolvedConflicts"
            // , "SatisfiedAssertions"
            // , "SeenEvents"
            // , "UnresolvedAnomalies"
            // , "UnresolvedConflicts"
            // , "ValidityChecksSatisfied" // BOOL ?
            // , "ViolatedAssertions"
            // Toreador
    };
    private final static String functions1[] = {
            // Toreador
            "Violations"
            , "PenaltyAmount"
    };
    private final static String functions2[] = {
            // Toreador
            "Counter"
    };

    static
    private HashMap<String, IDInfo>	 idTypeMapStatic;
    protected HashMap<String, IDInfo>  idTypeMap;
    protected String GLOB_Current_GTName = null;

    private Set<String> usedGTs = new HashSet<String>();
    private Set<String> declaredGTs = new HashSet<String>();
    private Set<String> usedActionGTs = new HashSet<String>();
    private Set<String> declaredActionGTs = new HashSet<String>();
    private void clearKnownGTsActions() {
        usedGTs.clear();
        declaredGTs.clear();
        usedActionGTs.clear();
        declaredGTs.clear();
    }
    private String actGTName(String a, String g) {return a+":"+g;}
    protected void usedGT(String n) { usedGTs.add(n); }
    protected void usedActionGT(String a, String g) { usedActionGTs.add(actGTName(a, g)); }
    protected void declaredGT(String n) { declaredGTs.add(n); }
    protected void declaredActionGT(String a, String g) { declaredActionGTs.add(actGTName(a, g)); }

    private String toPrism = ""
            , toLisp = ";; -*- mode: Lisp ; -*-\n\n"
            + "(progn\n  (in-package \"COMMON-LISP-USER\")\n\n";

    protected String prologue = ""
            , issueGuard  = "false" // Check that it's not empty at the end!!!
            , refuseGuard = "false"
            , revokeGuard = "false"
            , expireGuard = "false"
            /*
             * XXXGuardLisp is used to collect all the guards of the XXX
             * action so that it's used in the final negation.
             *
             * XXXGuardEachLisp is a guard that must be added to the guard of
             * each XXX transition, to simulate the composition with the
             * high-level lifecycle automaton (see module Lifecycle).
             * WHY NOT ADD IT TO THE FINAL NEGATED TRANSITION AS WELL? SHOULD
             * IT HAVE IT? SHOULD THE PRISM FINAL NEGATED TRANSITION ALSO HAVE
             * IT?
             *
             * XXXBasicActionEachLisp is an action that must be added to the
             * action of each XXX transition, to simulate the composition with
             * the high-level lifecycle automaton (see module Lifecycle).
             */
            , issueGuardLisp  = "	  (or\n"
            , refuseGuardLisp = "	  (or\n"
            , revokeGuardLisp = "	  (or\n"
            , expireGuardLisp = "	  (or\n"
            ;
    protected static final String
            issueGuardEachPrism =  "(llstate=slPreIssued)",
            refuseGuardEachPrism = "(llstate=slPreIssued)",
            revokeGuardEachPrism = "(llstate=slIssued)",
            expireGuardEachPrism = "(llstate=slIssued)",
            issueBasicActionEachPrism  = "(llstate'=slIssued)",
            refuseBasicActionEachPrism = "(llstate'=slRefused)",
            revokeBasicActionEachPrism = "(llstate'=slRevoked)",
            expireBasicActionEachPrism = "(llstate'=slPreIssued)",
            issueGuardEachLisp  = "(and\n		 (= *lstate* *slPreIssued*)",
            refuseGuardEachLisp = "(and\n		 (= *lstate* *slPreIssued*)",
            revokeGuardEachLisp = "(and\n		 (= *lstate* *slIssued*)",
            expireGuardEachLisp = "(and\n		 (= *lstate* *slIssued*)",
            issueBasicActionEachLisp  = "(setq *lstate* *slIssued* "
                    + " *issued* (1+ *issued*))\n",
            refuseBasicActionEachLisp = "(setq *lstate* *slRefused*)\n",
            revokeBasicActionEachLisp = "(setq *lstate* *slRevoked*)\n",
            expireBasicActionEachLisp = "(setq *lstate* *slPreIssued* "
                    + " *expired* (1+ *expired*))\n";

    static {
        // Ensure that the code runs with assertions enabled - refuse
        // to execute otherwise.
        boolean assertsEnabled = false;
        assert assertsEnabled = true; // Intentional side effect!!!
        if (!assertsEnabled)
            throw new java.lang.RuntimeException("Asserts must be enabled!!! Use -enableassertions");

        idTypeMapStatic = new HashMap<String, IDInfo>();
        String keywordsINT[] = {// Prism reserved keywords: ALL INT (HACK)
                //
                // A, bool, clock, const, ctmc, C, double, dtmc, E, endinit, endinvariant, endmodule, endrewards, endsystem, false, formula, filter, func, F, global, G, init, invariant, I, int, label, max, mdp, min, module, X, nondeterministic, Pmax, Pmin, P, probabilistic, prob, pta, rate, rewards, Rmax, Rmin, R, S, stochastic, system, true, U, W
                //
                "A"
                , "bool"
                , "clock"
                , "const"
                , "ctmc"
                , "C"
                , "double"
                , "dtmc"
                , "E"
                , "endinit"
                , "endinvariant"
                , "endmodule"
                , "endrewards"
                , "endsystem"
                , "false"
                , "formula"
                , "filter"
                , "func"
                , "F"
                , "global"
                , "G"
                , "init"
                , "invariant"
                , "I"
                , "int"
                , "label"
                , "max"
                , "mdp"
                , "min"
                , "module"
                , "X"
                , "nondeterministic"
                , "Pmax"
                , "Pmin"
                , "P"
                , "probabilistic"
                , "prob"
                , "pta"
                , "rate"
                , "rewards"
                , "Rmax"
                , "Rmin"
                , "R"
                , "S"
                , "stochastic"
                , "system"
                , "true"
                , "U"
                , "W"

                /* add our variables and constants */
                // // Functions
                // , "DetectedAnomalies"
                // , "DetectedConflicts"
                // , "ResolvedAnomalies"
                // , "ResolvedConflicts"
                // , "SatisfiedAssertions"
                // , "SeenEvents"
                // , "UnresolvedAnomalies"
                // , "UnresolvedConflicts"
                // , "ValidityChecksSatisfied" // BOOL ?
                // , "ViolatedAssertions"

                // Constants
                , "MaxInteger" // 50
                // , "MaxInterArrivalTime" // 10 // Using "TimeUnit" now

                , "slPreIssued"
                , "slIssued"
                , "slRefused"
                , "slRevoked"
                , "slEND"
                , "lstate"
                , "issued"
                , "expired"

                , "eNoEvent"
                , "eSeen"
                , "eTreated"
                , "event_state"

                , "smStart"
                , "smMonitorEvaluation"
                , "smAnomalyDetected"
                , "smAssertion"
                , "smAssertionSatisfied"
                , "smAssertionViolated"
                , "smFinal"
                , "mstate"

                , "saStart"
                , "saAnomalyResolved"
                , "saAnomalyUnresolved"
                , "astate"

        };
        for (String keyword : keywordsINT)
            idTypeMapStatic.put(keyword,
                    new IDInfo(keyword, ExpressionType.INT));
        // Certificate Lifecycle/SLA Manager Functions
        for (String f : functions0)
            idTypeMapStatic.put(f, new IDInfo(f, ExpressionType.INT));
        for (String f : functions1)
            idTypeMapStatic.put(f, new IDInfo(f, ExpressionType.INT));
        for (String f : functions2)
            idTypeMapStatic.put(f, new IDInfo(f, ExpressionType.INT));

        String keywordsBOOL[] = {"refused"
                , "revoked"
        };
        for (String keyword : keywordsBOOL)
            idTypeMapStatic.put(keyword,
                    new IDInfo(keyword, ExpressionType.BOOL));

        String keywordsCLOCK[] = {"Clock"
                , "interarrival"

        };
        for (String keyword : keywordsCLOCK)
            idTypeMapStatic.put(keyword,
                    new IDInfo(keyword, ExpressionType.CLOCK));

        String keywordsFLOAT[] = {"AnomalyDetectionP" // 0.1
                , "AssertionViolationP" // 0.1
                , "AnomalyResolutionP" // 0.8
        };
        for (String keyword : keywordsFLOAT)
            idTypeMapStatic.put(keyword,
                    new IDInfo(keyword, ExpressionType.FLOAT));

        String keywordsGTs[] = {"GuaranteeTermType", "GuardedActionType", "CustomBusinessValueType"};
    }
    public Translate () {
        idTypeMap = new HashMap<String, IDInfo>(idTypeMapStatic);
    }
    public Translate (Translate.TranslationMode mode) {
        this();
        setTranslationMode(mode);
    }
    public void setTranslationMode(Translate.TranslationMode m) {
        switch (m) {
            case VALIDATION:
                /* fall through */
            case NEGOTIATION_TIME:
                /* fall through */
            case NEGOTIATION_COST:
                translationMode = m; break;
            default:
                assert (false) : "Unkown PRISM translation mode!";
        }
    }

    public static void main(String[] args) throws Exception {
        String target = "Cms/z-test-input-for-validation.xml";
        if (args.length == 1) // Read the argument instead of a hard-coded file
            target = args[0];

        java.io.FileInputStream file = new java.io.FileInputStream(target);
        Translate tr = new Translate();

        String[] results
                = tr.translateSLAtoPrismAndLisp(file
                ,
                // TranslationMode.VALIDATION
                // TranslationMode.NEGOTIATION_TIME
                TranslationMode.NEGOTIATION_COST
        );
        System.out.println(results[0]); // Prism model
        System.err.println(results[1]); // Lisp code: follows validation model!
    }
    protected static String scaleToPrism(String s, long scale) {
        return (scale == 1) ? s : ("(" + scale + "*" + s + ")");
    }
    protected static String scaleToLisp(String s, long scale) {
        return (scale == 1) ? s : ("(* " + scale + " " + s + ")");
    }
    protected static String scaleToLispNanoSeconds(String s, long scale) {
        return (scale == 1)
                ? ("(* 1000000000 "		      + s + ")")
                : ("(* 1000000000 " + scale + " " + s + ")");
    }
    protected static String scaleToLisp(String s, long scale
            , ExpressionType mytype
            , ExpressionType othertype) {
        return ((mytype != ExpressionType.CLOCK
                && othertype == ExpressionType.CLOCK))
                ? scaleToLispNanoSeconds(s, scale)
                : scaleToLisp(s, scale);
    }

    // public void ExecuteSLA(java.io.InputStream SLAinput) throws Exception{
    //	   try {
    //	    translateSLAtoPrismAndLisp(SLAinput);
    //	    // Only start the SLA Manager if everything went fine with
    //	    // the translation.
    //	    Thread SLAManager = new Thread( new SLAManager() );
    //	    SLAManager.start();
    //	} catch (JAXBException e) {
    //	    throw new AssertionError("Failed to parse the WS_Agreement - reason: ", e);
    //	}
    // }

    public String[] translateSLAtoPrismAndLisp(java.io.InputStream SLAinput)
            throws JAXBException, Exception {
        return translateSLAtoPrismAndLisp(SLAinput
                , TranslationMode.VALIDATION );
    }
    public String[] translateSLAtoPrismAndLisp(java.io.InputStream SLAinput
            , Translate.TranslationMode mode)
            throws JAXBException, Exception {
        setTranslationMode(mode);

        clearKnownGTsActions();
        String[] result = new String[2];
        // synchronized (Translate.class) { if(completed) return; }
        // JAXBContext jc = JAXBContext.newInstance("myPackageName");
        JAXBContext jc =
                AccessController.doPrivileged(new PrivilegedAction<JAXBContext>() {
                    public JAXBContext run() {
                        try {
                            // JAXBContext jc1 = JAXBContext.newInstance("org.cumulus.certificate.model.monitoring:org.cumulus.certificate.model.lifecycle:org.cumulus.certificate.model");
                            JAXBContext jc1 = JAXBContext.newInstance("org.cumulus.certificate.model:org.ggf.schemas.graap._2007._03.ws_agreement");
                            assert (jc1!=null) : "JAXBContext.newInstance returned null";
                            return jc1;
                        } catch (JAXBException e) {
                            throw new AssertionError("Failed to create a new instance of the WS_Agreement - reason: ", e);
                        }
                    }
                });
        assert (jc!=null) : "jc is null";
        //Create unmarshaller
        final Unmarshaller um = jc.createUnmarshaller();
        assert (um!=null) : "jc.createUnmarshaller returned null";
        //Unmarshal XML contents of the file myDoc.xml into your Java object instance.
        Terms cmt =
                AccessController.doPrivileged(new PrivilegedAction<Terms>() {
                    public Terms run() {
                        try {
                            Object o = um.unmarshal(SLAinput);
                            JAXBElement<AgreementType> a=(JAXBElement<AgreementType>)o;
                            Terms cmt1 = new Terms();
                            cmt1.setAll(new All());
                            List<Terms> l =
                                    new java.util.ArrayList<Terms>();
                            switch (translationMode) {
                                case NEGOTIATION_TIME:
                                    /* fall through */
                                case VALIDATION:
                                    // Validation - we only care of Pending Terms!
                                {
                                    AgreedType pendingTerms
                                            = (AgreedType)a.getValue().getPending();
                                    List<Terms> pendingAgreedWithList
                                            = pendingTerms.getTerms();
                                    l.addAll(pendingAgreedWithList);
                                }
                                break;
                                case NEGOTIATION_COST:
                                {
                                    AgreedType agreedTerms
                                            = (AgreedType)a.getValue().getAgreed();
                                    List<Terms> agreedAgreedWithList
                                            = agreedTerms.getTerms();
                                    AgreedType pendingTerms
                                            = (AgreedType)a.getValue().getPending();
                                    List<Terms> pendingAgreedWithList
                                            = pendingTerms.getTerms();
                                    l.addAll(agreedAgreedWithList);
                                    l.addAll(pendingAgreedWithList);
                                }
                                break;
                                default:
                                    assert (false) : "Unkown PRISM translation mode!";
                            }
                            List<Object> termsOfCmt1 = cmt1.getAll()
                                    .getExactlyOnesAndOneOrMoresAndAlls();
                            // Join all the terms from t & cmt1:
                            for (Terms t : l) {
                                List<Object> termsOfT = t.getAll()
                                        .getExactlyOnesAndOneOrMoresAndAlls();
                                // XXXX BUG:
                                // getExactlyOnesAndOneOrMoresAndAlls does not
                                // return just a list of terms/GTs, since it's
                                // actually forming a tree of terms, with
                                // nodes like
                                // All/ExactlyOne/OneorMore/etc... :-(
                                termsOfCmt1.addAll(termsOfT);
                            }
                            // Terms cmt1 = (Terms)a.getValue().getTerms();
                            assert (cmt1!=null) : "um.unmarshal returned null";
                            return cmt1;
                        } catch (JAXBException e) {
                            throw new AssertionError("Failed to unmarshal the provided file \"" + SLAinput + "\" - reason: ", e);
                        }
                    }
                });
        ReflectiveVisitor eval = new Evaluator(this);

        Terms lc = cmt;
        assert (lc!=null) : "cmt.getGuaranteeTerms returned null";
        Visitable lcycle = new VisitableImpl(cmt);
        EvalNode node = lcycle.accept(eval);
        // Check that GT names used (Violations(_gt_),
        // PenaltyAmount(_gt_), counter(action,_gt_)) have been
        // declared.
        usedGTs.removeAll(declaredGTs);
        if (! usedGTs.isEmpty()) {
            String badNames = "===> WS Agreement uses undeclared GT names\n(check <asrt:Violations>, <asrt:PenaltyAmount>, <asrt:Counter>): <===";
            for (String s : usedGTs)
                badNames += "\n\t===> " + s + " <===";
            throw new Exception(badNames);
        }
        // Check that (Action, GT) name pairs used (counter(_action_,
        // _gt_)) have been declared.
        usedActionGTs.removeAll(declaredActionGTs);
        if (! usedActionGTs.isEmpty()) {
            String badNames = "===> WS Agreement uses undeclared (Action,GT) name pairs\n(Check action names in <asrt:Counter><asrt:Id name=\"ActionName\"/><asrt:Id name=\"GTname\"/></asrt:Counter>): <===";
            for (String s : usedActionGTs)
                badNames += "\n\t===> " + s + " <===";
            throw new Exception(badNames);
        }
        clearKnownGTsActions();
        // All names declared - continue translation.
        this.toPrism =
                "// -*- mode: c++ ; -*-\n" +
                        "ctmc // A Continuous-Time Markov Chain model\n\n" +
                        "const double minute = 60;\n" +
                        "const double hour = 60*minute;\n" +
                        "const double day = 1;//24*hour;//set day as time unit\n" +
                        "const double week = 7*day;\n" +
                        "const double year = 365*day;\n" +
                        "const double month = year/12;//an approximation\n\n" +
                        //	    "// const int k; // SLA variable\n" +
                        //	    "// const int Xm; // represents amount in properties\n" +
                        //	    "// const int T; // represents time in properties\n" +
                        "const int MaxInteger; // = max_i(var_i+1);\n\n" +
                        node.toPrism;
        this.toLisp +=
                // "#|(defstruct transition\n"
                // + "	  (name \"\") ; default is empty string\n"
                // + "	  (guard (lambda () nil)) ; default is false (nil)\n"
                // + "	  (action (lambda () nil))) ; default does nothing\n\n"
                //
                // + "(defparameter ***transitions*** nil \"Sequence of life-cycle transitions.\")\n\n"
                //
                // + "(defconstant ***System*** (jclass \"java.lang.System\"))\n"
                // + "(defconstant ***nanoTime*** (jmethod ***System*** \"nanoTime\"))\n"
                // + "(defun update-time () (setq ***now*** (/ (jcall ***nanoTime*** 0) 1000000000))) ;; 0 was nil - no idea what's going on...\n"
                // + "(defparameter ***now*** (update-time) \"Current time in nano-seconds.\")\n\n"
                //
                // + "(defun lifecycle-loop ()\n" +
                // "  (progn\n" +
                // "    (update-time) ;; used by both guard and actions\n" +
                // "    (let ((tr (find-if (lambda (x)\n" +
                // "		    (funcall (transition-guard x)))\n" +
                // "		  ***transitions***)))\n" +
                // "      (when tr\n" +
                // "   (format t \"~&;;;; Executing transition \\\"~A\\\"~%\" (transition-name tr))\n" +
                // "   (funcall (transition-action tr))))))\n\n"
                //
                // + "(defun test-transitions ()\n" +
                // "  (progn\n" +
                // "    (update-time)\n" +
                // "    (do* ((trs ***transitions*** (cdr trs))\n" +
                // "	  (tr (car trs) (car trs)))\n" +
                // "	 ((not tr) 'done)\n" +
                // "	 (format t \"~&;;;; Name: [~A] Guard: [~A] Action: [~A]~%\"\n" +
                // "		 (transition-name tr)\n" +
                // "		 (funcall (transition-guard tr))\n" +
                // "		 (funcall (transition-action tr))))))\n"
                //
                // + "|#\n";
                ";;; (add-to-classpath \"../bin/translator.jar\")\n\n"
                        + ";; (defconstant ***SLAManager*** (jclass \"Translator.SLAManager\"))\n"
        ;

        for ( String f : functions0 ) {
            this.toLisp +=
                    ";; (defconstant ***m" + f + "*** (jmethod ***SLAManager*** \"" + f + "\"))\n"
                            + ";; (defmacro ***" + f + "*** () (jcall ***m" + f + "*** nil))\n"
            ;
        }
        for ( String f : functions1 ) {
            this.toLisp +=
                    ";; (defconstant ***m" + f + "*** (jmethod ***SLAManager*** \"" + f + "\" \"java.lang.String\"))\n"
                            + ";; (defmacro ***" + f + "*** (gt) (jcall ***m" + f + "*** nil gt))\n"
            ;
        }
        for ( String f : functions2 ) {
            this.toLisp +=
                    ";; (defconstant ***m" + f + "*** (jmethod ***SLAManager*** \"" + f + "\" \"java.lang.String\" \"java.lang.String\"))\n"
                            + ";; (defmacro ***" + f + "*** (action gt) (jcall ***m" + f + "*** nil action gt))\n"
            ;
        }

        this.toLisp += // "\n(defconstant *slPreIssued* 0)\n"
                // + "(defconstant *slIssued* 1)\n"
                // + "(defconstant *slRefused* 2)\n"
                // + "(defconstant *slRevoked* 3)\n"
                // // + "(defconstant *slEND* 4)\n" // not needed in Lisp.
                // + "(defparameter *lstate* *slPreIssued*)\n"
                // + "(defparameter *issued* 0)\n"
                // + "(defparameter *expired* 0)\n\n"
                // +
                node.toLisp
                        + ")";

        // synchronized (Translate.class) { completed = true; }

        this.toLisp += ";;;; Lisp:\n";

        result[0] = this.toPrism;
        result[1] = this.toLisp;
        return result;
    }
}
//
enum ExpressionType {
    NONE, LIFECYCLE
    , TRANSITION
    , CLOCK
    , BOOL, INT, FLOAT, ID
    , SECONDS, MINUTES, HOURS, DAYS, WEEKS
    , DURATION_NO_CLOCKS, DURATION_CLOCKS, TERMS
}

class IDInfo {
    public String name;
    public ExpressionType type;
    IDInfo(String nm, ExpressionType tp) {name = nm; type = tp; }
}

class EvalNode { // All visit* methods should return an EvalNode
    /* what we output to Prism */
    public final String toPrism;
    public final String toPrismPrologue;  // TO DO write methods that combine nodes into new nodes
    /* what is the type of this statement/expression node */
    public final ExpressionType type;
    /* what is the minor type of this statement/expression node.

       When type is some duration unit (second, etc.) then the minor
       type is INT/FLOAT, depending on what the expression used were.
    */
    public final ExpressionType typeMinor;
    public final String toLisp;

    public EvalNode combine(String prefix
            , EvalNode node1
            , String infix
            , EvalNode node2
            , String postfix) {
        assert ((node1.type == node2.type)
                && (node1.typeMinor == node2.typeMinor)) : "combine - mismatched types";
        return new EvalNode(prefix
                + node1.toPrism
                + infix
                + node2.toPrism
                + postfix
                , node1.toPrismPrologue
                + node2.toPrismPrologue
                , node1.toLisp
                + node2.toLisp
                , node1.type
                , node1.typeMinor);
    }

    public EvalNode(String forPrism, String forLisp, ExpressionType theType) {
        toPrism = forPrism;
        toPrismPrologue = "";
        type = theType;
        typeMinor = theType;
        toLisp = forLisp;
    }

    public EvalNode(String forPrism, String forLisp, ExpressionType theType
            , ExpressionType theTypeMinor) {
        toPrism = forPrism;
        toPrismPrologue = "";
        type = theType;
        typeMinor = theTypeMinor;
        toLisp = forLisp;
    }

    public EvalNode(String forPrism, String forPrologue, String forLisp
            , ExpressionType theType
            , ExpressionType theTypeMinor) {
        toPrism = forPrism;
        toPrismPrologue = forPrologue;
        type = theType;
        typeMinor = theTypeMinor;
        toLisp = forLisp;
    }

}

/*
 * Using the reflective visitor pattern from:
 *
 * http://www.javaworld.com/article/2077602/learn-java/java-tip-98--reflect-on-the-visitor-design-pattern.html
 *
 */
interface ReflectiveVisitor {
    public EvalNode visit(Object o);
}

interface Visitable {
    Object getParam();
    EvalNode accept(ReflectiveVisitor visitor);
}

class VisitableImpl implements Visitable {
    private Object someParam;
    public VisitableImpl(Object param) { someParam = param; }
    @Override
    public Object getParam() { return someParam; }
    @Override
    public EvalNode accept(ReflectiveVisitor ev) {
        return ev.visit(this.getParam());
    }
}


class Evaluator implements ReflectiveVisitor {
    private Translate translator = null;
    public Evaluator(Translate tr) {translator = tr;}
    private String
            Q =
            // "\\\""
            "@"
            , U =
            // "\\\""
            "$"
            ;

    private String realName(String s) {
        return ((IDInfo) translator.idTypeMap.get(s)).name;
    }

    private String getClassName(Object type) {
        if (type==null) throw new RuntimeException("Null type argument");

        String className = type.getClass().getName();
        // Strip package info.
        return className.substring(className.lastIndexOf('.')+1);
    }

    public EvalNode visitObjectTerms(org.ggf.schemas.graap._2007._03.ws_agreement.Terms type) throws Exception {
        ReflectiveVisitor eval = new Evaluator(translator);

        String wsresult = "";
        String PrismGTViolationModules = "";
        String PrismGuardFormulas = "";
        String PrismVariableINCFormulas = "";
        String PrismVariableDeclarations = "";
        String PrismPenaltyAmountVariables = "";
        String PrismTransitionBodies = "";
        String PrismTransitionAssignments = "";
        String PrismRewardsDefinitions = "";
        String PrismTransitionBodyHead = "(SLAactive' = ! (false))\n";

        List<LispGTRec> LispGTRecs = new  java.util.ArrayList<LispGTRec>();
        List<GuaranteeTermType> listOfGts = new java.util.ArrayList<GuaranteeTermType>();
        String LispVars = "";
        String PrintVars = "\n(defun print-vars ()"
                + "\n  (concatenate 'string"
                ;

        for (Object termType
                : type.getAll().getExactlyOnesAndOneOrMoresAndAlls() ) {
            JAXBElement<TermType> trm = (JAXBElement<TermType>) termType;
            if (trm.getName().getLocalPart().equals("GuaranteeTerm")) {
                listOfGts.add((GuaranteeTermType)trm.getValue());
            }
        }

        for (GuaranteeTermType gto : listOfGts) {
            String gtname = gto.getName();
            translator.declaredGT(gtname);
            PrismVariableDeclarations += "violations_" + gtname
                    + " : [0 .. MaxInteger] init 0;\n";
            PrismTransitionBodies += "\n[" + gtname + "Violated]  SLAactive -> 1:";
            PrismTransitionAssignments = "";
            PrismTransitionBodyHead = "(SLAactive' = ! (false))\n"; // DEFAULT - RESET FOR EACH GT.
            LispVars += "\n(defparameter *violations-" + gtname + "* 0)";
            PrintVars += "\n  (format nil \"" + Q + "*violations-"
                    + gtname
                    + "*" + U + " : " + Q + "~A" + U + ",~%\" *violations-" + gtname + "*)";

            PrismGTViolationModules += "module "
                    + gtname +
                    "\n[" + gtname + "Violated] true -> " +gtname
                    + "ViolationRate : true;\n"
                    + "endmodule\n";
            PrismRewardsDefinitions += "\nrewards \""
                    + gtname + "Violations\" // expected number of times it is violated\n"
                    + "   [" + gtname + "Violated] true: 1; \nendrewards\n";

            List<CustomBusinessValueType> listOfGuards =
                    gto.getBusinessValueList().getCustomBusinessValues();
            for (CustomBusinessValueType grd : listOfGuards) {
                CustomBusinessValueType.Rate rate = grd.getRate(); // XXX
                Visitable rateV = new VisitableImpl(rate.getExprNumericalAbs().getValue());
                EvalNode rateNode = rateV.accept(eval);
                switch (translator.translationMode) {
                    case VALIDATION:
                        wsresult += "formula " +gtname + "ViolationRate = "
                                + rateNode.toPrism
                                + ";\n";
                        break;
                    case NEGOTIATION_TIME:
                        /* fall through */
                    case NEGOTIATION_COST:
                        wsresult += "const double " +gtname + "ViolationRate;\n";
                        break;
                    default:
                        assert (false) : "Unkown PRISM translation mode!";
                }
                List<GuardedActionType>listOfActions = grd.getGuardedActions();
                /* ONLY ONCE PER gtname ! */
                PrismVariableINCFormulas += "\nformula INCviolations_" + gtname
                        + " = (min(MaxInteger,violations_" + gtname + "+1));\n";

                List<LispActionRec> LispGTActionRecs
                        = new java.util.ArrayList<LispActionRec>();
                for(int y=0; y< listOfActions.size(); y++) {
                    TActionAbs actionA =
                            listOfActions.get(y).getActionAbs().getValue();
                    String action =
                            (actionA instanceof Penalty)
                                    ? "Penalty"
                                    : ((actionA instanceof ReNegotiate)
                                    ? "ReNegotiate"
                                    : ((Other) actionA).getActionName());
                    translator.declaredActionGT(action, gtname);

                    JAXBElement<? extends TExprBooleanAbs> exprJAXBElement =
                            listOfActions.get(y).getValueExpr().getExprBooleanAbs();
                    TExprBooleanAbs expr = exprJAXBElement.getValue();
                    Visitable pdw = new VisitableImpl(expr);
                    translator.GLOB_Current_GTName = gtname;
                    EvalNode pdecl = pdw.accept(eval);
                    translator.GLOB_Current_GTName = null;
                    PrismGuardFormulas += "\nformula guard_" + action + gtname
                            + " = "
                            + pdecl.toPrism
                            + ";";
                    String LispGTActionGuard = pdecl.toLisp;
                    String LispGTActionUpdate =
                            "\n	 (setq res (concatenate 'string res (format nil \" "
                                    + Q + "~A" + U + ",~%\" \"" + action + "\")))";
                    java.math.BigInteger penalty = java.math.BigInteger.ZERO;

                    if(!(actionA instanceof ReNegotiate)) {
                        PrismVariableDeclarations +=
                                "counter_" + action + "_" + gtname
                                        + " : [0 .. MaxInteger] init 0;\n";
                        LispVars += "\n(defparameter *counter-" + gtname
                                + "-" + action
                                + "* 0)";
                        PrintVars += "\n  (format nil \"" + Q + "*counter-"
                                + gtname + "-" + action
                                + "*" + U + " : " + Q + "~A" + U
                                + ", ~%\" *counter-"
                                + gtname + "-" + action + "*)";

                        PrismVariableINCFormulas += "formula INCcounter_"
                                + action + "_" + gtname +
                                "= (min(MaxInteger,counter_"
                                + action + "_" + gtname
                                + "+(guard_" + action + gtname +
                                "?1:0)));\n";
                        PrismTransitionAssignments +=
                                "\n & (counter_" + action + "_" + gtname
                                        + "'=INCcounter_" + action + "_" + gtname + ")";
                        LispGTActionUpdate += "\n	 (incf *counter-"
                                + gtname
                                + "-" + action
                                + "*)";

                        if (actionA instanceof Penalty) {
                            PrismPenaltyAmountVariables +=
                                    "const int "
                                            + "penalty_for_" + gtname + ";\n";
                            PrismVariableDeclarations +=
                                    "penalty_amount_" +  gtname
                                            + " : [0 .. MaxInteger] init 0;\n";
                            LispVars +=
                                    "\n(defparameter *penalty-amount-" + gtname
                                            + "-" + action
                                            + "* 0)";
                            PrintVars += "\n  (format nil \"" + Q
                                    + "*penalty-amount-"
                                    + gtname + "-" + action
                                    + "*" + U + " : "
                                    + Q + "~A" + U + ",~%\" *penalty-amount-"
                                    + gtname + "-" + action + "*)";

                            PrismVariableINCFormulas +=
                                    "formula INCpenalty_amount_"
                                            + gtname
                                            + " = (min(MaxInteger, penalty_amount_"
                                            + gtname
                                            + "+(guard_Penalty"
                                            + gtname
                                            + "?";
                            java.math.BigInteger val =
                                    ((Penalty) actionA).getValue();
                            penalty = val;
                            if (null == val) { // SHOULD NEVER HAPPEN
                                PrismVariableINCFormulas += "NULL";
                                throw new Exception("===> Penalty amount is null - MUST be an *integer* <===");
                            } else {
                                switch (translator.translationMode) {
                                    case VALIDATION:
                                        PrismVariableINCFormulas += val; break;
                                    case NEGOTIATION_TIME:
                                        /* fall through */
                                    case NEGOTIATION_COST:
                                        PrismVariableINCFormulas +=
                                                "penalty_for_" + gtname;
                                        break;
                                }
                            }
                            PrismVariableINCFormulas += ":0)));";
                            PrismTransitionAssignments +=
                                    "\n & (penalty_amount_" + gtname
                                            + "'=INCpenalty_amount_" + gtname + ")";
                            LispGTActionUpdate +=
                                    "\n	 (incf *penalty-amount-"
                                            + gtname + "* " + val + ")";
                        }    // End of Penalty action
                    }	      // End of Penalty/Other action
                    else {	// It's a ReNegotiate action.
                        PrismTransitionBodyHead =
                                "\n(SLAactive'=  ! (guard_"
                                        + action + gtname + "))\n";
                    }
                    LispActionRec lispActionRec
                            = new LispActionRec(action
                            , LispGTActionGuard
                            , LispGTActionUpdate
                            , penalty);
                    LispGTActionRecs.add(lispActionRec);
                } // FOR listOfActions[y]
                PrismTransitionBodies +=
                        PrismTransitionBodyHead
                                + " & (violations_" + gtname
                                + "'=INCviolations_" + gtname + ")"
                                + PrismTransitionAssignments
                                +"\n ;\n";

                LispGTRec lispGTRec = new LispGTRec(gtname, LispGTActionRecs);
                LispGTRecs.add(lispGTRec);

                // System.err.println("\nGT: "
                //		    + gtname
                //		    + "\n\tVars: " + LispVars
                //		    + "\n\tActions: " + LispGTActions
                //		    + "\n\tGTActionGuard: " + LispGTActionGuard
                //		    + "\n\tGTActionUpdate: " + LispGTActionUpdate
                //		    );
            }
        }
        String toLisp = LispVars + PrintVars + "))";
        toLisp += "\n\n(defun java-string-to-lisp-string (a-java-string)"
                +"\n  (let ((a-lisp-string (make-array 0"
                +"\n				   :element-type 'character"
                +"\n				   :fill-pointer 0"
                +"\n				   :adjustable t))"
                +"\n	(a-java-string-as-a-char-array (jcall \"toCharArray\" a-java-string)))"
                +"\n    (dotimes (i (jarray-length a-java-string-as-a-char-array)"
                +"\n	      a-lisp-string)"
                +"\n      (vector-push-extend (jarray-ref a-java-string-as-a-char-array i)"
                +"\n			  a-lisp-string))))\n";

        String LispMain = "\n\n(defun treat-gt-violation (a-java-string)"
                + "\n  (let ((res \"\")"
                + "\n	 (the-gt-symbol (intern (java-string-to-lisp-string a-java-string) 'common-lisp-user)))"
                + "\n    (case the-gt-symbol"
                ;

        for (LispGTRec gt : LispGTRecs) {
            toLisp += "\n(defconstant *GT-" + gt.name
                    + "* (intern \"" + gt.name + "\" 'common-lisp-user))";
            LispMain += "\n	 (|" + gt.name + "|"
                    + "\n	     (progn"
                    + "\n	       (incf *violations-" + gt.name + "*)"
                    + "\n	       (setq res (concatenate 'string res (format nil \"{" + Q + "GT" + U + " : " + Q + "~A" + U + ",  " + Q + "Actions" + U + " : [~%\" \"" + gt.name + "\")))";
            ;
            for (LispActionRec ac : gt.actions) {
                LispMain += "\n	      (when " + ac.guard
                        + " " + ac.action + ")";
            }
            LispMain += "))";
        }
        LispMain += "\n	     (otherwise (setq res (concatenate 'string res (format nil \"{" + Q + "error" + U + " : " + Q + "Unknown GT ~A!" + U + "}~%\" the-gt-symbol)))))"
                +"\n    (setq res (concatenate 'string (substitute #\\SPACE #\\, res :count 1 :from-end t) \"], \" (substitute #\\SPACE #\\, (print-vars) :count 1 :from-end t) \"}\"))"
                +"\n    res #+nil(jnew \"java.lang.String\" res)))\n";
        toLisp += LispMain;

        return new EvalNode(wsresult + translator.prologue
                + ((Translate.TranslationMode.VALIDATION
                == translator.translationMode)
                ? ""
                : PrismPenaltyAmountVariables)
                + "\n//Environment - Monitoring module auto-derived\n"
                + PrismGTViolationModules
                + "\n//SLA Manager module\n\n"
                + "// guard_ActionGT renames violations_GT to INCviolations_GT (all other variables are kept the same)"
                + PrismGuardFormulas
                + PrismVariableINCFormulas
                + "\nmodule SLA_Manager\nSLAactive : bool init true;\n"
                + PrismVariableDeclarations
                + PrismTransitionBodies
                + "endmodule\n" + "\nrewards \"time\" \n   true : 1;\nendrewards\n"
                + PrismRewardsDefinitions
                , toLisp // XXXX
                , ExpressionType.TERMS);
    }

    public EvalNode visitObjectTStateTransitionModel(org.cumulus.certificate.model.TStateTransitionModel type) {
        ReflectiveVisitor eval = new Evaluator(translator);

        String result = "";
        String resultLisp = "";

        TTimeUnit timeUnitXML = type.getTimeUnit();
        assert (null != timeUnitXML) : "Model MUST contain a TimeUnit declaration to set the time granularity!";
        translator.timeUnit = visit((TExprNumericalAbs)
                timeUnitXML.getExprNumericalAbs().getValue());
        EvalNode timeUnitCode = translator.timeUnit;
        assert isDurationType(timeUnitCode.type)
                : "TimeUnit MUST have some time unit (Seconds/Minutes/Hours/Days/Weeks).";
        result += "\nconst int TimeUnit = "
                + timeUnitCode.toPrism
                + "; // Unit is "
                + timeUnitCode.type
                + "\n\n";
        resultLisp += "#+nil(defconstant *time-unit* "
                + timeUnitCode.toLisp
                + ") ;; Unit is "
                + timeUnitCode.type
                + " - it's ignored in Lisp\n\n";

        List<JAXBElement<? extends TParDecl>> listOfParameters
                = type.getParameters().getTParDeclAbs();

        result += "// List of Parameters";
        for (int i=0; i < listOfParameters.size(); ++i) {
            TParDecl param = listOfParameters.get(i).getValue();
            Visitable pdw = new VisitableImpl(param);
            EvalNode pdecl = pdw.accept(eval);
            result = result + "\n" + pdecl.toPrism;
            resultLisp = resultLisp + "\n" + pdecl.toLisp;
        }
        result = result + "\n" +
                "const int eNoEvent = 0;\n" +
                "const int eSeen = 1;\n" +
                "const int eTreated = 2;\n";
        String result2 = "\nmodule Certificate_Manager" +
                "\n llstate: [slPreIssued .. slRevoked] init slPreIssued;\n";
        //    System.err.println(result); // DEBUG
        //    System.err.println(translator.prologue);
        List<JAXBElement<? extends TVarDecl>> listOfVariables
                = type.getVariables().getTVarDeclAbs();

        result2 = result2 + "\n // List of Variables\n" +
                "event_state: [eNoEvent .. eTreated] init eNoEvent;\n" +
                "SeenEvents: [0 .. MaxInteger] init 0;\n" +
                "ViolatedAssertions: [0 .. MaxInteger] init 0;\n" +
                "SatisfiedAssertions: [0 .. MaxInteger] init 0;\n" +
                "DetectedAnomalies: [0 .. MaxInteger] init 0;\n" +
                "ResolvedAnomalies: [0 .. MaxInteger] init 0;\n" +
                "UnresolvedAnomalies: [0 .. MaxInteger] init 0;\n";
        for (int j=0; j<listOfVariables.size(); j++) {
            TVarDecl variable = listOfVariables.get(j).getValue();
            Visitable pdw = new VisitableImpl(variable);
            EvalNode vdecl = pdw.accept(eval);
            result2 += "\n" + vdecl.toPrism;
            resultLisp += "\n" + vdecl.toLisp;
        }
        // Add [tick] to increment all local clocks
        result2 += "\n\n // advance all local clocks when Clock advances\n";
        String tickingTwice = "[tickTwice] true ->\n	   (event_state'=event_state)";
        String tickingOnce =  "[tickOnce]  true ->\n	   (event_state'=event_state)";
        final String tick_prefix = "\n	     & ";
        for (HashMap.Entry<String, IDInfo> entry
                : translator.idTypeMap.entrySet()) {
            String key = entry.getKey();
            IDInfo val = entry.getValue();
            if ( (val.type == ExpressionType.CLOCK)
                    && (! key.equals("Clock"))
                    && (! key.equals("interarrival"))
                    ) {
                tickingTwice += tick_prefix
                        + "(" + val.name
                        + "'=((" + val.name + "+2*TimeUnit>=MaxInteger)?MaxInteger:("
                        + val.name + "+2*TimeUnit)))";
                tickingOnce  += tick_prefix
                        + "(" + val.name
                        + "'=((" + val.name + "+TimeUnit>=MaxInteger)?MaxInteger:("
                        + val.name + "+TimeUnit)))";
            }
        }
        tickingTwice += ";\n";
        tickingOnce += ";\n";
        result2 += tickingTwice + tickingOnce;

        result2 += "\n //List of fixed Transitions\n" +
                "[anomalyDetected]	    " + //"(state!=sFinal) & " +
                "(event_state=eNoEvent)\n" +
                "			    -> (event_state' = eSeen)\n" +
                "			    & (DetectedAnomalies' = ((DetectedAnomalies=MaxInteger)?MaxInteger:(DetectedAnomalies+1)))\n" +
                "			    & (SeenEvents' = ((SeenEvents=MaxInteger)?SeenEvents:(SeenEvents+1)));\n" +
                "[anomalyResolved]	    "+ //"(state!=sFinal) &"
                "(event_state=eNoEvent)\n" +
                "			    -> (event_state' = eSeen)\n" +
                "			    & (ResolvedAnomalies' = ((ResolvedAnomalies=MaxInteger)?MaxInteger:(ResolvedAnomalies+1)));\n" +
                "[anomalyUnresolved]    " + //"(state!=sFinal) & " +
                "(event_state=eNoEvent)\n" +
                "			    -> (event_state' = eSeen)\n" +
                "			    & (UnresolvedAnomalies' = ((UnresolvedAnomalies=MaxInteger)?MaxInteger:(UnresolvedAnomalies+1)));\n" +
                "[assertionSatisfied]   " + //"(state!=sFinal) & " +
                "(event_state=eNoEvent)\n" +
                "			    -> (event_state' = eSeen)\n" +
                "			    & (SatisfiedAssertions' = ((SatisfiedAssertions=MaxInteger)?MaxInteger:(SatisfiedAssertions+1)))\n" +
                "			    & (SeenEvents' = ((SeenEvents=MaxInteger)?SeenEvents:(SeenEvents+1)));\n" +
                "[assertionViolated]    " + //"(state!=sFinal) & " +
                "(event_state=eNoEvent)\n" +
                "			    -> (event_state' = eSeen)\n" +
                "			    & (ViolatedAssertions' = ((ViolatedAssertions=MaxInteger)?MaxInteger:(ViolatedAssertions+1)))\n"  +
                "			    & (SeenEvents' = ((SeenEvents=MaxInteger)?SeenEvents:(SeenEvents+1)));\n";

        List<JAXBElement<? extends TTrDecl>> listOfTransitions
                = type.getTransitions().getTTrDeclAbs();

        result2 += "\n\n // List of customised Transitions";
        resultLisp += "\n\n(progn\n";
        for (int t=0; t < listOfTransitions.size(); t++){
            TTrDecl transition = listOfTransitions.get(t).getValue();
            Visitable pdw = new VisitableImpl(transition);
            EvalNode trans = pdw.accept(eval);
            result2 += "\n" + trans.toPrism;
            resultLisp += "\n" + trans.toLisp;
        }
        resultLisp += " (setq ***transitions*** (reverse ***transitions***)))\n\n";

        assert (! translator.issueGuard.equals("false"))
                : "Model *must* contain at least one issue transition";
        /*
         * The Lisp code shouldn't do anything here - just iterate again.
         */
        result2 +=
                "\n[]		(event_state=eSeen)\n" +
                        "//****************************************************************\n" +
                        "//* Should these be combined with the Lifecycle module's guards?!?\n" +	    "//* And what should the combination be?!?!\n" +
                        "//****************************************************************\n" +
                        "		    & ! (  // no issue\n" +
                        translator.issueGuard +
                        ")\n	       & ! (  // no refuse\n" +
                        translator.refuseGuard +
                        ")\n	       & ! (  // no revoke\n" +
                        translator.revokeGuard +
                        ")\n	       & ! (  // no expire\n" +
                        translator.expireGuard +
                        ")\n	       -> (event_state'= eTreated);\n" +
                        "[event_treated]	    (event_state = eTreated) -> (event_state' = eNoEvent);\n" +
                        "endmodule\n"
        ;
        return new EvalNode(result + translator.prologue + result2
                , resultLisp
                , ExpressionType.LIFECYCLE);
    }

    /*
     * Parameters: all should extend TParDecl.
     */
    public EvalNode visitTParDeclDefault(Object type) {
        System.err.print("AAARGH!!! " + type.getClass().getName());
        // System.err.println("--> Should never be here!!");
        assert false : " --> Should never be here!!";
        return new EvalNode("", "", ExpressionType.NONE);
    }

    private void updateIdTypeMap(String s, ExpressionType t) {
        assert (! translator.idTypeMap.containsKey(s))
                : "ID " + s + " already exists";
        //	System.err.println("TYPE: Adding ID " + s + " with type " + t);
        translator.idTypeMap.put(s, new IDInfo("u_" + s, t));
    }

    public EvalNode visitTParDeclBooleanParam(org.cumulus.certificate.model.BooleanParam type) {
        ExpressionType t = ExpressionType.BOOL;
        String error = "BOOL";
        EvalNode initial = visit((TExprBooleanAbs) type.getInitialValue()
                .getExprBooleanAbs().getValue());
        assert (initial.type == t) : "Type should be " + error;
        updateIdTypeMap(type.getVariableName(), t);
        return new EvalNode("const bool "
                +	realName(type.getVariableName())
                +	" = "
                +	initial.toPrism
                + ";"
                , "(defconstant "
                +	realName(type.getVariableName())
                +	" "
                +	initial.toLisp
                + ")"
                , initial.type);
    }
    //
    private EvalNode visitHelperParam(String name
            , String prismType
            , ExpressionType rt
            , EvalNode initial) {
        assert (initial.type == rt) : "Type should be "
                + ((prismType.equals("double")) ? "float" : prismType);
        updateIdTypeMap(name, rt);
        return new EvalNode("const "
                + prismType
                + " "
                +	realName(name)
                +	" = "
                +	initial.toPrism
                + ";"
                , "(defconstant "
                +	realName(name)
                +	" "
                +	initial.toLisp
                + ")"
                , initial.type);
    }

    public EvalNode visitTParDeclIntParam(org.cumulus.certificate.model.IntParam type) {
        ExpressionType rt = ExpressionType.INT;
        EvalNode initial = visit((TExprNumericalAbs) type.getInitialValue()
                .getExprNumericalAbs().getValue());
        return visitHelperParam(type.getVariableName()
                , "int"
                , rt
                , initial);
    }

    public EvalNode visitTParDeclFloatParam(org.cumulus.certificate.model.FloatParam type) {
        ExpressionType rt = ExpressionType.FLOAT;
        EvalNode initial = visit((TExprNumericalAbs) type.getInitialValue()
                .getExprNumericalAbs().getValue());
        return visitHelperParam(type.getVariableName()
                , "double"
                , rt
                , initial);
    }
    //
    private Boolean isDurationType(ExpressionType rt) {
        return (rt.ordinal() >= ExpressionType.SECONDS.ordinal())
                && (rt.ordinal() <= ExpressionType.WEEKS.ordinal());
    }

    private EvalNode visitHelperParamDuration(String name
            , EvalNode initial) {

        assert isDurationType(initial.type)
                : "Type of " + name + " must be a duration";
        assert ((initial.typeMinor == ExpressionType.INT) // MUST BE INT!
                &&
                (initial.typeMinor == ExpressionType.INT
                        || initial.typeMinor == ExpressionType.FLOAT))
                // : "Minor type should be either INT or FLOAT";
                : "Minor type should be INT"; // Prism wants integer durations.
        long scaleToTimeUnit = combineTypesAdd("DurationParam"
                , translator.timeUnit.type
                , initial.type
                , translator.timeUnit.typeMinor
                , initial.typeMinor).scale1;
        updateIdTypeMap(name, initial.type);
        return new EvalNode("const "
                + ( (initial.typeMinor == ExpressionType.INT)
                ? "int "
                : "double " )
                +	realName(name)
                +	" = "
                +	initial.toPrism
                + "; // Unit was "
                + initial.type
                , "(defconstant "
                +  realName(name)
                +  " "
                +  Translate.scaleToLisp(initial.toLisp,
                scaleToTimeUnit)
                + ") ; Unit was "
                + initial.type
                , initial.type
                , initial.typeMinor);
    }

    public EvalNode visitTParDeclDurationParam(org.cumulus.certificate.model.DurationParam type) {
        // ExpressionType rt = ExpressionType.SECONDS;
        EvalNode initial = visit((TExprNumericalAbs) type.getInitialValue()
                .getExprNumericalAbs().getValue());
        return visitHelperParamDuration(type.getVariableName()
                , initial
        );
    }

    /*
     * Variables: all should extend TVarDecl, like parameters extend TParDecl.
     */
    public EvalNode visitTVarDeclDefault(Object type) {
        System.err.print("AAARGH!!! " + type.getClass().getName());
        // System.err.println("--> Should never be here!!");
        assert false : " --> Should never be here!!";
        return new EvalNode("", "", ExpressionType.NONE);
    }


    public EvalNode visitTVarDeclBooleanVar(org.cumulus.certificate.model.BooleanVar type) {
        ExpressionType t = ExpressionType.BOOL;
        String error = "BOOL";
        EvalNode initial = visit((TExprBooleanAbs) type.getInitialValue()
                .getExprBooleanAbs().getValue());
        assert (initial.type == t) : "Type should be " + error;
        updateIdTypeMap(type.getVariableName(), t);
        return new EvalNode(realName(type.getVariableName())
                + " : bool init "
                + initial.toPrism
                + ";"
                , "(defparameter "
                +	realName(type.getVariableName())
                +	" "
                +	initial.toLisp
                + ")"
                , initial.type);
    }
    //
    private EvalNode visitHelperVar(String name
            , String prismType
            , ExpressionType rt
            , EvalNode initial
            , EvalNode min
            , EvalNode max) {
        assert (initial.type == rt) : "Type should be "
                + ((prismType.equals("double")) ? "float" : prismType);
        assert (min.type == rt) : "Type should be "
                + ((prismType.equals("double")) ? "float" : prismType);
        assert (max.type == rt) : "Type should be "
                + ((prismType.equals("double")) ? "float" : prismType);
        updateIdTypeMap(name, rt);
        return new EvalNode(realName(name)
                + " : ["
                + min.toPrism
                + " .. "
                + max.toPrism
                + "] init "
                + initial.toPrism
                + ";"
                , "(defparameter "
                +	realName(name)
                +	" "
                +	initial.toLisp
                + " #| ["
                + min.toPrism
                + " .. "
                + max.toPrism
                + "] - no type info in Lisp|# )"
                , initial.type);
    }

    public EvalNode visitTVarDeclIntVar(org.cumulus.certificate.model.IntVar type) {
        ExpressionType rt = ExpressionType.INT;
        EvalNode initial = visit((TExprNumericalAbs) type.getInitialValue()
                .getExprNumericalAbs().getValue());
        EvalNode min = visit((TExprNumericalAbs) type.getMin()
                .getExprNumericalAbs().getValue());
        EvalNode max = visit((TExprNumericalAbs) type.getMax()
                .getExprNumericalAbs().getValue());
        return visitHelperVar(type.getVariableName()
                , "int"
                , rt
                , initial
                , min
                , max);
    }

    public EvalNode visitTVarDeclFloatVar(org.cumulus.certificate.model.FloatVar type) {
        ExpressionType rt = ExpressionType.FLOAT;
        EvalNode initial = visit((TExprNumericalAbs) type.getInitialValue()
                .getExprNumericalAbs().getValue());
        EvalNode min = visit((TExprNumericalAbs) type.getMin()
                .getExprNumericalAbs().getValue());
        EvalNode max = visit((TExprNumericalAbs) type.getMax()
                .getExprNumericalAbs().getValue());
        return visitHelperVar(type.getVariableName()
                , "double"
                , rt
                , initial
                , min
                , max);
    }
    //
    private EvalNode visitHelperVarDuration(String name
            , EvalNode initial
            , EvalNode min
            , EvalNode max) {
        assert isDurationType(min.type) : "Type of " + name + "'s min value must be a duration";
        assert isDurationType(initial.type) : "Type of " + name + "'s initial value must be a duration";
        assert isDurationType(max.type) : "Type of " + name + "'s max value must be a duration";
        EvalNode minUnit = min;
        if (minUnit.type.ordinal() > max.type.ordinal())
            minUnit = max;
        if (minUnit.type.ordinal() > initial.type.ordinal())
            minUnit = initial;
        assert (initial.typeMinor == ExpressionType.INT)
                : "Initial should not be a floating point duration";
        assert (min.typeMinor == ExpressionType.INT)
                : "Min should not be a floating point duration";
        assert (max.typeMinor == ExpressionType.INT)
                : "Max should not be a floating point duration";
        updateIdTypeMap(name, minUnit.type);
        long minScale = combineTypesAdd("DurationVar"
                , minUnit.type
                , min.type
                , minUnit.typeMinor
                , min.typeMinor).scale1;
        long maxScale = combineTypesAdd("DurationVar"
                , minUnit.type
                , max.type
                , minUnit.typeMinor
                , max.typeMinor).scale1;
        long initialScale = combineTypesAdd("DurationVar"
                , minUnit.type
                , initial.type
                , minUnit.typeMinor
                , initial.typeMinor).scale1;
        long scaleToTimeUnit =
                combineTypesAdd("DurationVar"
                        , translator.timeUnit.type
                        , initial.type
                        , translator.timeUnit.typeMinor
                        , initial.typeMinor).scale1;
        String mintoPrism = Converted.scaleToPrism(min.toPrism, minScale);
        String maxtoPrism = Converted.scaleToPrism(max.toPrism, maxScale);
        String initialtoPrism = Converted.scaleToPrism(initial.toPrism, initialScale);
        return new EvalNode(realName(name)
                + " : ["
                + mintoPrism
                + " .. "
                + maxtoPrism
                + "] init "
                + initialtoPrism
                + "; // Units were min:"
                + min.type + "/max:" + max.type + "/initial:" + initial.type
                , "(defparameter "
                +  realName(name)
                // +  " (* "
                // +       scaleToNanoSeconds
                // +       " "
                // +       scaleToLisp(initial.toLisp, scaleToNanoSeconds)
                // + ") #| ["
                +  " "
                +  Translate.scaleToLisp(initial.toLisp,
                scaleToTimeUnit)
                +  " #| ["
                +  mintoPrism
                +  " .. "
                +  maxtoPrism
                +  "] : " + initialtoPrism + " - no type info in Lisp - Units were min:"
                +  min.type + "/max:" + max.type + "/initial:" + initial.type
                + "|#)"
                , minUnit.type
                , minUnit.typeMinor);
    }

    public EvalNode visitTVarDeclDurationVar(org.cumulus.certificate.model.DurationVar type) {
        EvalNode initial = visit((TExprNumericalAbs) type.getInitialValue()
                .getExprNumericalAbs().getValue());
        EvalNode min = visit((TExprNumericalAbs) type.getMin()
                .getExprNumericalAbs().getValue());
        EvalNode max = visit((TExprNumericalAbs) type.getMax()
                .getExprNumericalAbs().getValue());
        return visitHelperVarDuration(type.getVariableName()
                , initial
                , min
                , max
        );
    }

    public EvalNode visitTVarDeclClockVar(org.cumulus.certificate.model.ClockVar type) {
        updateIdTypeMap(type.getVariableName(), ExpressionType.CLOCK);
        return new EvalNode(realName(type.getVariableName())
                + " : [0 .. MaxInteger] init 0; //PTA: clock; // Counts in what?!?\n"
                , "(defparameter "
                + realName(type.getVariableName())
                + " ***now***)\n"
                , ExpressionType.CLOCK
                // all clocks are in this unit.
                , translator.timeUnit.type);
    }
    //
    public EvalNode visitTVarDeclEnumerationVar(org.cumulus.certificate.model.EnumerationVar type) {
        List<String> ids = type.getVS();
        String first = ids.get(0);
        String last = ids.get(ids.size()-1);
        String initial = type.getInitialValue();

        String result = "";
        String constprologue = "";
        String toLisp = "";

        for (int i=0; i < ids.size(); ++i){
            String id = ids.get(i);
            updateIdTypeMap(id, ExpressionType.INT);
            constprologue += "const int "
                    + realName(id)
                    + " = "
                    + i
                    + ";\n";
            toLisp += "(defconstant "
                    + realName(id)
                    + " "
                    + i
                    + ")\n";
        }
        translator.prologue += constprologue;
        updateIdTypeMap(type.getVariableName(), ExpressionType.INT);
        String start = ( (initial == null)? "u_" + first : "u_" + initial );
        result += realName(type.getVariableName())
                + " : ["
                + "u_" + first
                + " .. "
                + "u_" + last
                + "] init "
                + start
                + ";";
        return new EvalNode(result
                , constprologue
                , toLisp
                + "(defparameter "
                +	 realName(type.getVariableName())
                +	 " "
                +	 start
                +	 " #| [u_"
                +	 first
                +	 " .. u_"
                +	 last
                +	 "] - no type info in Lisp|# )"
                , ExpressionType.INT, ExpressionType.INT);
    }

    /* Transitions*/

    public EvalNode visitTTranDeclDefault(Object type) {
        System.err.print("AAARGH!!! " + type.getClass().getName());
        // System.err.println("--> Should never be here!!");
        assert false : " --> Should never be here!!";
        return new EvalNode("", "(error \"UKNOWN INPUT\")"
                , ExpressionType.NONE);
    }

    private EvalNode visitTTRDeclHelper(String name
            , String toPrismGuard
            , String toPrismAssign
            , String toLispGuardEach
            , String toLispGuard
            , String toLispAssignEach
            , String toLispAssign) {
        return new EvalNode("["
                +	name
                +	"]	 (event_state=eSeen)\n		  & "
                +	toPrismGuard
                +	"\n		  -> "
                +	toPrismAssign
                +	" (event_state' = eTreated);"
                , ";;; (eval-when (:execute :compile-toplevel :load-toplevel)\n"
                + "(push (make-transition\n	      :name \""
                +	name
                +	"\" "
                + "\n	:guard (lambda ()\n		"
                +			toLispGuardEach
                +			"\n		 "
                +				 toLispGuard + ")"
                + "\n	:action (lambda ()\n		 (progn"
                + "\n		   "
                +			   toLispAssignEach
                +			   toLispAssign
                +	    ")))\n"
                + "	     ***transitions***)\n"
                , ExpressionType.TRANSITION);
    }

    public EvalNode visitTTrDeclIssue(org.cumulus.certificate.model.Issue type) {
        String transitionLabel = getClassName(type);
        TExprBooleanAbs guard = type.getGuard().getExprBooleanAbs().getValue();
        EvalNode guardEval = visit(guard);
        List<TAssignment> assigns = type.getAssigns();
        String assignsString = "";
        String assignsStringLisp = "";
        for (int i = 0; i < assigns.size(); ++i) {
            EvalNode assign = visit(assigns.get(i));
            assignsString += assign.toPrism;
            assignsStringLisp += assign.toLisp;
        }
        // There might be multiple issue transitions
        translator.issueGuard += " | ((" + translator.issueGuardEachPrism + ") & (" + guardEval.toPrism + "))";
        translator.issueGuardLisp += "\n"
                + translator.issueGuardEachLisp
                + "\n     "
                + guardEval.toLisp
                + ")";
        return visitTTRDeclHelper(transitionLabel.toLowerCase()
                , "((" + translator.issueGuardEachPrism + ") & (" + guardEval.toPrism + "))"
                , translator.issueBasicActionEachPrism + " & " + assignsString
                , translator.issueGuardEachLisp
                , guardEval.toLisp + ")"
                , translator.issueBasicActionEachLisp
                , assignsStringLisp);
    }

    public EvalNode visitTTrDeclRefuse(org.cumulus.certificate.model.Refuse type) {
        String transitionLabel = getClassName(type);
        TExprBooleanAbs guard = type.getGuard().getExprBooleanAbs().getValue();
        EvalNode guardEval = visit(guard);
        List<TAssignment> assigns = type.getAssigns();
        String assignsString = "";
        String assignsStringLisp = "";
        for (int i = 0; i < assigns.size(); ++i) {
            EvalNode assign = visit(assigns.get(i));
            assignsString += assign.toPrism;
            assignsStringLisp += assign.toLisp;
        }
        // There might be multiple refuse transitions
        translator.refuseGuard += " | ((" + translator.refuseGuardEachPrism + ") & (" + guardEval.toPrism + "))";
        translator.refuseGuardLisp += "\n"
                + translator.refuseGuardEachLisp
                + "\n     "
                + guardEval.toLisp
                + ")";
        return visitTTRDeclHelper(transitionLabel.toLowerCase()
                , "((" + translator.refuseGuardEachPrism + ") & (" + guardEval.toPrism + "))"
                , translator.refuseBasicActionEachPrism + " & " + assignsString
                , translator.refuseGuardEachLisp
                , guardEval.toLisp + ")"
                , translator.refuseBasicActionEachLisp
                , assignsStringLisp);
    }

    public EvalNode visitTTrDeclRevoke(org.cumulus.certificate.model.Revoke type) {
        String transitionLabel = getClassName(type);
        TExprBooleanAbs guard = type.getGuard().getExprBooleanAbs().getValue();
        EvalNode guardEval = visit(guard);
        List<TAssignment> assigns = type.getAssigns();
        String assignsString = "";
        String assignsStringLisp = "";
        for (int i = 0; i < assigns.size(); ++i) {
            EvalNode assign = visit(assigns.get(i));
            assignsString += assign.toPrism;
            assignsStringLisp += assign.toLisp;
        }
        // There might be multiple revoke transitions
        translator.revokeGuard += " | ((" + translator.revokeGuardEachPrism + ") & (" + guardEval.toPrism + "))";
        translator.revokeGuardLisp += "\n"
                + translator.revokeGuardEachLisp
                + "\n     "
                + guardEval.toLisp
                + ")";
        return visitTTRDeclHelper(transitionLabel.toLowerCase()
                , "((" + translator.revokeGuardEachPrism + ") & (" + guardEval.toPrism + "))"
                , translator.revokeBasicActionEachPrism + " & " + assignsString
                , translator.revokeGuardEachLisp
                , guardEval.toLisp + ")"
                , translator.revokeBasicActionEachLisp
                , assignsStringLisp);
    }

    public EvalNode visitTTrDeclExpire(org.cumulus.certificate.model.Expire type) {
        String transitionLabel = getClassName(type);
        TExprBooleanAbs guard = type.getGuard().getExprBooleanAbs().getValue();
        EvalNode guardEval = visit(guard);
        List<TAssignment> assigns = type.getAssigns();
        String assignsString = "";
        String assignsStringLisp = "";
        for (int i = 0; i < assigns.size(); ++i) {
            EvalNode assign = visit(assigns.get(i));
            assignsString += assign.toPrism;
            assignsStringLisp += assign.toLisp;
        }
        // There might be multiple expire transitions
        translator.expireGuard += " | ((" + translator.expireGuardEachPrism + ") & (" + guardEval.toPrism + "))";
        translator.expireGuardLisp += "\n"
                + translator.expireGuardEachLisp
                + "\n     "
                + guardEval.toLisp
                + ")";
        return visitTTRDeclHelper(transitionLabel.toLowerCase()
                , "((" + translator.expireGuardEachPrism + ") & (" + guardEval.toPrism + "))"
                , translator.expireBasicActionEachPrism + " & " + assignsString
                , translator.expireGuardEachLisp
                , guardEval.toLisp + ")"
                , translator.expireBasicActionEachLisp
                , assignsStringLisp);
    }

    public EvalNode visitTTrDeclOther(org.cumulus.certificate.model.Other type) {
        String trnm = type.getName();
        assert (!(trnm.equals("issue")
                || trnm.equals("expire")
                || trnm.equals("revoke")
                || trnm.equals("refuse"))) : "Transition names issue/expire/refuse/revoke are special - use the special transition types";
        //   String transitionLabel = getClassName(type);
        TExprBooleanAbs guard = type.getGuard().getExprBooleanAbs().getValue();
        EvalNode guardEval = visit(guard);
        List<TAssignment> assigns = type.getAssigns();
        String assignsString = "";
        String assignsStringLisp = "";
        for (int i = 0; i < assigns.size(); ++i) {
            EvalNode assign = visit(assigns.get(i));
            assignsString += assign.toPrism;
            assignsStringLisp += assign.toLisp;
        }
        // There might be multiple other transitions
        return visitTTRDeclHelper("user_" + trnm
                , guardEval.toPrism
                , assignsString
                , "(and "
                , guardEval.toLisp
                , ""
                , assignsStringLisp);
    }

    public EvalNode visitObjectTAssignment(org.cumulus.certificate.model.TAssignment type) {
        String name = realName(type.getVariableName());
        String assignEval = "";
        String assignEvalLisp = "";
        if (type.getClockReset() != null){
            assignEval = "0";
            assignEvalLisp = "***now***";
        }
        else if (type.getExprBooleanAbs() != null){
            EvalNode assign = visit(type.getExprBooleanAbs().getValue());
            assignEval = assign.toPrism;
            assignEvalLisp = assign.toLisp;
        }
        else {
            EvalNode assign = visit(type.getExprNumericalAbs().getValue());
            assignEval = assign.toPrism;
            assignEvalLisp = assign.toLisp;
        }
        return new EvalNode("("
                +	name
                +	"' = "
                +	assignEval
                + ")\n	       &"
                , "\n		   (setq "
                +	name
                +	" "
                +	assignEvalLisp
                + ")"
                , ExpressionType.BOOL);
    }
    //
    public EvalNode visitTExprBooleanAbsTrue(org.cumulus.certificate.model.True type) {
        return new EvalNode("true"
                , "\n		     t"
                , ExpressionType.BOOL);
    }

    public EvalNode visitTExprBooleanAbsFalse(org.cumulus.certificate.model.False type) {
        return new EvalNode("false"
                , "\n		     nil"
                , ExpressionType.BOOL);
    }

    public EvalNode visitTExprBooleanAbsAnd(org.cumulus.certificate.model.And type) {
        List<JAXBElement<? extends TExprBooleanAbs>> args
                = type.getExprBooleanAbs();
        if (0 == args.size())
            return new EvalNode("true"
                    , "t"
                    , ExpressionType.BOOL);
        TExprBooleanAbs firstArg = args.get(0).getValue();
        EvalNode operand = visit(firstArg);
        String result = "(" + operand.toPrism;
        String resultLisp = "(and\n		 " + operand.toLisp;
        for (int i = 1; i < args.size(); ++i) {
            operand = visit(args.get(i).getValue());
            result += "\n		& " + operand.toPrism;
            resultLisp += "\n		 " + operand.toLisp;
        }
        result += ")";
        resultLisp += ")";
        // System.err.println("And: " + result);
        return new EvalNode(result
                , resultLisp
                , ExpressionType.BOOL);
    }

    public EvalNode visitTExprBooleanAbsOr(org.cumulus.certificate.model.Or type) {
        List<JAXBElement<? extends TExprBooleanAbs>> args
                = type.getExprBooleanAbs();
        if (0 == args.size())
            return new EvalNode("false"
                    , "nil"
                    , ExpressionType.BOOL);
        TExprBooleanAbs firstArg = args.get(0).getValue();
        EvalNode operand = visit(firstArg);
        String result = "(" + operand.toPrism;
        String resultLisp = "(or\n		 " + operand.toLisp;
        for (int i = 1; i < args.size(); ++i) {
            operand = visit(args.get(i).getValue());
            result += "\n		| " + operand.toPrism;
            resultLisp += "\n		 " + operand.toLisp;
        }
        result += ")";
        resultLisp += ")";
        // System.err.println("And: " + result);
        return new EvalNode(result
                , resultLisp
                , ExpressionType.BOOL);
    }

    public EvalNode visitTExprBooleanAbsEqualTo(org.cumulus.certificate.model.EqualTo type) {
        List<JAXBElement<?>> args = type.getExprNumericalAbs();
        assert (args.size() == 2) : "EqualTo needs two arguments";
        EvalNode arg0 = visit(args.get(0).getValue());
        EvalNode arg1 = visit(args.get(1).getValue());
        Converted res = combineTypesAdd("EqualTo",
                arg0.type,	arg1.type,
                arg0.typeMinor, arg1.typeMinor);
        assert !( (arg0.type == ExpressionType.CLOCK)
                && (arg1.type == ExpressionType.CLOCK) )
                : "EqualTo: Prism digital clocks cannot be compared to each other";

        String toPrism = "("
                + res.toPrism0(arg0.toPrism)
                + " = "
                + res.toPrism1(arg1.toPrism)
                + ")";
        String toLisp = " (= "
                + Converted.scaleToLisp(arg0.toLisp, res.scale0, arg0.type, arg1.type)
                + " "
                + Converted.scaleToLisp(arg1.toLisp, res.scale1, arg1.type, arg0.type)
                + ")";
        return new EvalNode(toPrism
                , toLisp
                , ExpressionType.BOOL);
    }

    /*
     * Equality and inequality operators have some constraints that
     * exist because of Prism's PTA engines - see here:
     * http://www.prismmodelchecker.org/manual/ThePRISMLanguage/PTAs
     *
     * - Digital engine:
     *
     * -- clocks can only be compared using non-strict operators: =, <=, >=
     *
     * -- clocks constraints cannot refer to more than one clock
     *
     * Constraints not imposed currently:
     *
     * - All engines:
     * -- "references to clocks must appear as *conjunctions* of *simple
     *	   clock constraints*, i.e. conjunctions of expressions of the
     *	   form x~c or x~y where x and y are clocks, c is an
     *	   integer-valued expression and ~ is one of <, <=, >=, >, =."
     *	  So we cannot have negated clock constraints, nor disjoined
     *	  clock constraints.
     *
     * -- "PTAs should not exhibit timelocks, i.e. the possibility of
     *	   reaching a state where no transitions are possible and time
     *	   cannot elapse beyond a certain point (due to invariant
     *	   conditions). PRISM checks for timelocks and reports an error
     *	   if one is found."
     *	  Here we depend on PRISM to report an error.
     *
     * -- "PTAs should be well-formed and non-zeno (see e.g. [KNSW07]
     *	   for details). Currently, PRISM does not check automatically
     *	   that these assumptions are satisfied. "
     *	  We don't plan to ever check for this - if PRISM cannot do it...
     *
     * - Stochastic Games and Backwards Reachability engines:
     * -- "Modules cannot read the local variables of other modules
     *	   and global variables are not permitted."
     *	  Our models have no global variables and the language does
     *	  not allow one to declare global variables.
     */
    public EvalNode visitTExprBooleanAbsUnequalTo(org.cumulus.certificate.model.UnequalTo type) {
        List<JAXBElement<?>> args = type.getExprNumericalAbs();
        assert (args.size() == 2) : "UnequalTo needs two arguments";
        EvalNode arg0 = visit(args.get(0).getValue());
        EvalNode arg1 = visit(args.get(1).getValue());
        Converted res = combineTypesAdd("UnequalTo",
                arg0.type,	arg1.type,
                arg0.typeMinor, arg1.typeMinor);
        assert !( (arg0.type == ExpressionType.CLOCK)
                && (arg1.type == ExpressionType.CLOCK) )
                : "UnequalTo: Prism digital clocks cannot be compared to each other";
        assert !( (arg0.type == ExpressionType.CLOCK)
                || (arg1.type == ExpressionType.CLOCK) )
                : "UnequalTo: Prism digital clocks constraint - cannot compare a clock for inequality";

        String toPrism = "("
                + res.toPrism0(arg0.toPrism)
                + " != "
                + res.toPrism1(arg1.toPrism)
                + ")";
        String toLisp = " (!= "
                + Converted.scaleToLisp(arg0.toLisp, res.scale0, arg0.type, arg1.type)
                + " "
                + Converted.scaleToLisp(arg1.toLisp, res.scale1, arg1.type, arg0.type)
                + ")";
        return new EvalNode(toPrism
                , toLisp
                , ExpressionType.BOOL);
    }

    public EvalNode visitTExprBooleanAbsLessThan(org.cumulus.certificate.model.LessThan type) {
        List<JAXBElement<?>> args = type.getExprNumericalAbs();
        assert (args.size() == 2) : "LessThan needs two arguments";
        EvalNode arg0 = visit(args.get(0).getValue());
        EvalNode arg1 = visit(args.get(1).getValue());
        Converted res = combineTypesAdd("LessThan",
                arg0.type,	arg1.type,
                arg0.typeMinor, arg1.typeMinor);
        assert !( (arg0.type == ExpressionType.CLOCK)
                && (arg1.type == ExpressionType.CLOCK) )
                : "LessThan: Prism digital clocks cannot be compared to each other";
        assert !( (arg0.type == ExpressionType.CLOCK)
                || (arg1.type == ExpressionType.CLOCK) )
                : "LessThan: Prism digital clocks constraint - use LessThanEqualTo instead of LessThan";

        String toPrism = "("
                + res.toPrism0(arg0.toPrism)
                + " < "
                + res.toPrism1(arg1.toPrism)
                + ")";
        String toLisp = " (< "
                + Converted.scaleToLisp(arg0.toLisp, res.scale0, arg0.type, arg1.type)
                + " "
                + Converted.scaleToLisp(arg1.toLisp, res.scale1, arg1.type, arg0.type)
                + ")";
        return new EvalNode(toPrism
                , toLisp
                , ExpressionType.BOOL);
    }

    public EvalNode visitTExprBooleanAbsLessThanEqualTo(org.cumulus.certificate.model.LessThanEqualTo type) {
        List<JAXBElement<?>> args = type.getExprNumericalAbs();
        assert (args.size() == 2) : "LessThanEqualTo needs two arguments";
        EvalNode arg0 = visit(args.get(0).getValue());
        EvalNode arg1 = visit(args.get(1).getValue());
        Converted res = combineTypesAdd("LessThanEqualTo",
                arg0.type,	arg1.type,
                arg0.typeMinor, arg1.typeMinor);
        assert !( (arg0.type == ExpressionType.CLOCK)
                && (arg1.type == ExpressionType.CLOCK) )
                : "LessThanEqualTo: Prism digital clocks cannot be compared to each other";

        String toPrism = "("
                + res.toPrism0(arg0.toPrism)
                + " <= "
                + res.toPrism1(arg1.toPrism)
                + ")";
        String toLisp = " (<= "
                + Converted.scaleToLisp(arg0.toLisp, res.scale0, arg0.type, arg1.type)
                + " "
                + Converted.scaleToLisp(arg1.toLisp, res.scale1, arg1.type, arg0.type)
                + ")";
        return new EvalNode(toPrism
                , toLisp
                , ExpressionType.BOOL);
    }

    public EvalNode visitTExprBooleanAbsGreaterThan(org.cumulus.certificate.model.GreaterThan type) {
        List<JAXBElement<?>> args = type.getExprNumericalAbs();
        assert (args.size() == 2) : "GreaterThan needs two arguments";
        EvalNode arg0 = visit(args.get(0).getValue());
        EvalNode arg1 = visit(args.get(1).getValue());
        Converted res = combineTypesAdd("GreaterThan",
                arg0.type,	arg1.type,
                arg0.typeMinor, arg1.typeMinor);
        assert !( (arg0.type == ExpressionType.CLOCK)
                && (arg1.type == ExpressionType.CLOCK) )
                : "GreaterThan: Prism digital clocks cannot be compared to each other";
        assert !( (arg0.type == ExpressionType.CLOCK)
                || (arg1.type == ExpressionType.CLOCK) )
                : "GreaterThan: Prism digital clocks constraint - use GreaterThanEqualTo instead of GreaterThan";

        String toPrism = "("
                + res.toPrism0(arg0.toPrism)
                + " > "
                + res.toPrism1(arg1.toPrism)
                + ")";
        String toLisp = " (> "
                + Converted.scaleToLisp(arg0.toLisp, res.scale0, arg0.type, arg1.type)
                + " "
                + Converted.scaleToLisp(arg1.toLisp, res.scale1, arg1.type, arg0.type)
                + ")";
        return new EvalNode(toPrism
                , toLisp
                , ExpressionType.BOOL);
    }

    public EvalNode visitTExprBooleanAbsGreaterThanEqualTo(org.cumulus.certificate.model.GreaterThanEqualTo type) {
        List<JAXBElement<?>> args = type.getExprNumericalAbs();
        assert (args.size() == 2) : "GreaterThanEqualTo needs two arguments";
        EvalNode arg0 = visit(args.get(0).getValue());
        EvalNode arg1 = visit(args.get(1).getValue());
        Converted res = combineTypesAdd("GreaterThanEqualTo",
                arg0.type,	arg1.type,
                arg0.typeMinor, arg1.typeMinor);
        assert !( (arg0.type == ExpressionType.CLOCK)
                && (arg1.type == ExpressionType.CLOCK) )
                : "GreaterThanEqualTo: Prism digital clocks cannot be compared to each other";

        String toPrism = "("
                + res.toPrism0(arg0.toPrism)
                + " >= "
                + res.toPrism1(arg1.toPrism)
                + ")";
        String toLisp = " (>= "
                + Converted.scaleToLisp(arg0.toLisp, res.scale0, arg0.type, arg1.type)
                + " "
                + Converted.scaleToLisp(arg1.toLisp, res.scale1, arg1.type, arg0.type)
                + ")";
        return new EvalNode(toPrism
                , toLisp
                , ExpressionType.BOOL);
    }
    //
    public EvalNode visitTExprNumericalAbsInt(org.cumulus.certificate.model.Int type) {
        return new EvalNode(type.getValue().toString()
                , type.getValue().toString()
                , ExpressionType.INT);
    }

    public EvalNode visitTExprNumericalAbsFloat(org.cumulus.certificate.model.Float type) {
        return new EvalNode("" + type.getValue()
                , "" + type.getValue()
                , ExpressionType.FLOAT);
    }

    public EvalNode visitTExprNumericalAbsId(org.cumulus.certificate.model.Id type) {
        // System.err.println("ID name is \"" + type.getName() + "\"");
        IDInfo nameInfo = (IDInfo) (translator.idTypeMap.get(type.getName()));
        assert (null!=nameInfo)
                : ("ID \""
                + type.getName()
                + "\" has no associated type"
                + " - typo/forgotten declaration?");
        ExpressionType typeInfo = nameInfo.type;
        String nm = realName(type.getName());
        return new EvalNode((typeInfo == ExpressionType.CLOCK)
                // ? ("(" + nm + "*TimeUnit)") // scale time
                ? (nm) // scale time
                : nm
                , (typeInfo == ExpressionType.CLOCK)
                ? "(- ***now*** " + nm + ")"
                : nm
                , typeInfo
                , (typeInfo == ExpressionType.CLOCK)
                ? translator.timeUnit.type
                : ( (isDurationType(typeInfo))
                ? ExpressionType.INT
                : typeInfo )
        );
    }

    public EvalNode visitTExprNumericalAbsSeconds(org.cumulus.certificate.model.Seconds type) {
        EvalNode result = visit(type.getExprNumericalAbs().getValue());
        return new EvalNode(result.toPrism
                , result.toLisp
                , ExpressionType.SECONDS
                , result.typeMinor);
    }

    public EvalNode visitTExprNumericalAbsMinutes(org.cumulus.certificate.model.Minutes type) {
        EvalNode result = visit(type.getExprNumericalAbs().getValue());
        // System.err.println("Minutes constructor: minor type is " + result.typeMinor);
        return new EvalNode(result.toPrism
                , result.toLisp
                , ExpressionType.MINUTES
                , result.typeMinor);
    }

    public EvalNode visitTExprNumericalAbsHours(org.cumulus.certificate.model.Hours type) {
        EvalNode result = visit(type.getExprNumericalAbs().getValue());
        return new EvalNode(result.toPrism
                , result.toLisp
                , ExpressionType.HOURS
                , result.typeMinor);
    }

    public EvalNode visitTExprNumericalAbsDays(org.cumulus.certificate.model.Days type) {
        EvalNode result = visit(type.getExprNumericalAbs().getValue());
        return new EvalNode("(" + result.toPrism + "/day)"
                , result.toLisp
                , ExpressionType.DAYS
                , result.typeMinor);
    }

    public EvalNode visitTExprNumericalAbsWeeks(org.cumulus.certificate.model.Weeks type) {
        EvalNode result = visit(type.getExprNumericalAbs().getValue());
        return new EvalNode(result.toPrism
                , result.toLisp
                , ExpressionType.WEEKS
                , result.typeMinor);
    }

    class Converted extends Translate {
        public final ExpressionType resType;
        public final ExpressionType resTypeMinor;
        public final long scale0;
        public final long scale1;
        public Converted(ExpressionType r) {
            resType = r;
            resTypeMinor = r;
            scale0 = 1;
            scale1 = 1;
        }
        public Converted(ExpressionType r, ExpressionType rM) {
            resType = r;
            resTypeMinor = rM;
            scale0 = 1;
            scale1 = 1;
        }
        public Converted(ExpressionType r,ExpressionType rM,long sc0,long sc1) {
            resType = r;
            resTypeMinor = rM;
            scale0 = sc0;
            scale1 = sc1;
        }
        public String toPrism0(String s) {
            return scaleToPrism(s, scale0);
        }
        public String toPrism1(String s) {
            return scaleToPrism(s, scale1);
        }
        public String toLisp0(String s) {
            return scaleToLisp(s, scale0);
        }
        public String toLisp1(String s) {
            return scaleToLisp(s, scale1);
        }
    }
    private Converted combineTypesAdd(String inFunction,
                                      ExpressionType typeA,
                                      ExpressionType typeB,
                                      ExpressionType typeAMinor,
                                      ExpressionType typeBMinor) {
        Boolean swappedMajor = false;
        ExpressionType type1 = typeA
                , type2 = typeB
                , type1Minor = typeAMinor
                , type2Minor = typeBMinor;
        if (typeA.ordinal() > typeB.ordinal()) {
            type1 = typeB;
            type2 = typeA;
            type1Minor = typeBMinor;
            type2Minor = typeAMinor;
            swappedMajor = true;
        }

        switch (type1) {
            case CLOCK:
                // System.err.println("Clock 1's minor type is " + type1Minor);
                assert (type1Minor == translator.timeUnit.type)
                        : (inFunction
                        + ": Clock's minor type different from timeUnit's");
                switch (type2) {
                    case CLOCK:
                        // System.err.println("Clock 2's minor type is " + type2Minor);
                        assert (type2Minor == translator.timeUnit.type)
                                : (inFunction
                                + ": Clock's minor type different from timeUnit's");
                        return new Converted(ExpressionType.CLOCK);
                    default:
                        // System.err.println("combineTypesAdd: swappedMajor = "
                        //		   + swappedMajor);
                        // System.err.println("Clock 1's minor type is " + type1Minor);
                        // System.err.println("type 2's type is " + type2);
                        // System.err.println("type 2's minor type is " + type2Minor);
                        // System.exit(1);
                        Converted res = combineTypesAdd(inFunction
                                , translator.timeUnit.type
                                , type2
                                , ExpressionType.INT
                                , swappedMajor
                                        ? type1Minor
                                        : type2Minor);
                        return new Converted(// swappedMajor ? type2 : type1
                                /* Clocks taint all duration types */
                                ExpressionType.CLOCK
                                , // swappedMajor ? type1 : type2
                                translator.timeUnit.type
                                , swappedMajor ? res.scale1 : res.scale0
                                , swappedMajor ? res.scale0 : res.scale1);
                }
            case INT:
                switch (type2) {
                    case INT:
                        return new Converted(ExpressionType.INT);
                    case FLOAT:
                        return new Converted(ExpressionType.FLOAT);
                    default:
                        assert type1 == type2 : inFunction + ": Types " + type1 + " and " + type2 + " do not match";
                }
                break;
            case FLOAT:
                switch (type2) {
                    case INT:
                        return new Converted(ExpressionType.FLOAT);
                    case FLOAT:
                        return new Converted(ExpressionType.FLOAT);
                    default:
                        assert (type1 == type2) : inFunction + ": Types " + type1 + " and " + type2 + " do not match";
                }
                break;
            case SECONDS:
                switch (type2) {
                    case SECONDS:
                        return new Converted(ExpressionType.SECONDS,
                                combineTypesAdd(inFunction
                                        , type1Minor
                                        , type2Minor
                                        , type1Minor
                                        , type2Minor).resType);
                    case MINUTES:
                        return new Converted(ExpressionType.SECONDS,
                                combineTypesAdd(inFunction
                                        , type1Minor
                                        , type2Minor
                                        , type1Minor
                                        , type2Minor).resType,
                                // convert minutes to seconds
                                swappedMajor ? 60 :  1,
                                swappedMajor ?  1 : 60);
                    default:
                        Converted res = combineTypesAdd(inFunction, // ask MINUTES
                                ExpressionType.MINUTES,
                                type2,
                                type1Minor,
                                type2Minor);
                        return new Converted(ExpressionType.SECONDS,
                                res.resTypeMinor,
                                (swappedMajor? 60*res.scale1 :res.scale0),
                                (swappedMajor? res.scale0 :60*res.scale1));
                }
            case MINUTES:
                switch (type2) {
                    case MINUTES:
                        return new Converted(ExpressionType.MINUTES,
                                combineTypesAdd(inFunction
                                        , type1Minor
                                        , type2Minor
                                        , type1Minor
                                        , type2Minor).resType);
                    case HOURS:
                        return new Converted(ExpressionType.MINUTES,
                                combineTypesAdd(inFunction
                                        , type1Minor
                                        , type2Minor
                                        , type1Minor
                                        , type2Minor).resType,
                                // convert hours to minutes
                                swappedMajor ? 60 :  1,
                                swappedMajor ?  1 : 60);
                    default:
                        Converted res = combineTypesAdd(inFunction, // ask HOURS
                                ExpressionType.HOURS,
                                type2,
                                type1Minor,
                                type2Minor);
                        return new Converted(ExpressionType.MINUTES,
                                res.resTypeMinor,
                                (swappedMajor? 60*res.scale1 :res.scale0),
                                (swappedMajor? res.scale0 :60*res.scale1));
                }
            case HOURS:
                switch (type2) {
                    case HOURS:
                        return new Converted(ExpressionType.HOURS,
                                combineTypesAdd(inFunction
                                        , type1Minor
                                        , type2Minor
                                        , type1Minor
                                        , type2Minor).resType);
                    case DAYS:
                        return new Converted(ExpressionType.HOURS,
                                combineTypesAdd(inFunction
                                        , type1Minor
                                        , type2Minor
                                        , type1Minor
                                        , type2Minor).resType,
                                // convert hours to minutes
                                swappedMajor ? 24 :  1,
                                swappedMajor ?  1 : 24);
                    default:
                        Converted res = combineTypesAdd(inFunction, // ask DAYS
                                ExpressionType.DAYS,
                                type2,
                                type1Minor,
                                type2Minor);
                        return new Converted(ExpressionType.HOURS,
                                res.resTypeMinor,
                                (swappedMajor? 24*res.scale1 :res.scale0),
                                (swappedMajor? res.scale0 :24*res.scale1));
                }
            case DAYS:
                switch (type2) {
                    case DAYS:
                        return new Converted(ExpressionType.DAYS,
                                combineTypesAdd(inFunction
                                        , type1Minor
                                        , type2Minor
                                        , type1Minor
                                        , type2Minor).resType);
                    case WEEKS:
                        return new Converted(ExpressionType.DAYS,
                                combineTypesAdd(inFunction
                                        , type1Minor
                                        , type2Minor
                                        , type1Minor
                                        , type2Minor).resType,
                                // convert hours to minutes
                                swappedMajor ?  7 :  1,
                                swappedMajor ?  1 :  7);
                    default:
                        Converted res = combineTypesAdd(inFunction, // ask WEEKS
                                ExpressionType.WEEKS,
                                type2,
                                type1Minor,
                                type2Minor);
                        return new Converted(ExpressionType.HOURS,
                                res.resTypeMinor,
                                (swappedMajor? 7*res.scale1 : res.scale0),
                                (swappedMajor? res.scale0 : 7*res.scale1));
                }
            case WEEKS:
                switch (type2) {
                    case WEEKS:
                        return new Converted(ExpressionType.WEEKS,
                                combineTypesAdd(inFunction
                                        , type1Minor
                                        , type2Minor
                                        , type1Minor
                                        , type2Minor).resType);
                    default:		// No other time unit to ask - fail.
                        assert (type2 == ExpressionType.WEEKS)
                                : inFunction + ": A duration unit can only be converted to another duration unit";
                }
                break;

            default:
                assert (type1 == type2) : inFunction + ": Types " + type1 + " and " + type2 + " do not match";
        }
        return new Converted(ExpressionType.NONE);
    }
    private Converted combineTypesMult(String inFunction,
                                       ExpressionType typeA,
                                       ExpressionType typeB,
                                       ExpressionType typeAMinor,
                                       ExpressionType typeBMinor) {
        Boolean swappedMajor = false;
        ExpressionType type1 = typeA
                , type2 = typeB
                , type1Minor = typeAMinor
                , type2Minor = typeBMinor;
        if (typeA.ordinal() > typeB.ordinal()) {
            type1 = typeB;
            type2 = typeA;
            type1Minor = typeBMinor;
            type2Minor = typeAMinor;
            swappedMajor = true;
        }

        switch (type1) {
            case CLOCK:
                // System.err.println("Clock 1's minor type is " + type1Minor);
                assert (type1Minor == translator.timeUnit.type)
                        : (inFunction
                        + ": Clock's minor type different from timeUnit's");
                switch (type2) {
                    case INT:
                        return new Converted(ExpressionType.CLOCK, ExpressionType.INT);
                    default:
                        assert (type2==ExpressionType.INT)
                                : (inFunction
                                + ": Clocks can be multiplied with INTs only");
                        // return this;
                }
            case INT:
                switch (type2) {
                    case INT:
                        return new Converted(ExpressionType.INT);
                    case FLOAT:
                        return new Converted(ExpressionType.FLOAT);
                    case SECONDS:
                    case MINUTES:
                    case HOURS:
                    case DAYS:
                    case WEEKS:
                        return new Converted(type2, type2Minor);
                    default:
                        assert type1 == type2 : inFunction + ": Cannot multiply INT with " + type2;
                }
                break;
            case FLOAT:
                switch (type2) {
                    case INT:
                        return new Converted(ExpressionType.FLOAT);
                    case FLOAT:
                        return new Converted(ExpressionType.FLOAT);
                    case SECONDS:
                    case MINUTES:
                    case HOURS:
                    case DAYS:
                    case WEEKS:
                        /*
                         * Duration expressions should be INT but one can then
                         * floor/ceil it, so we should accept multiplication
                         * by a float.
                         */
                        return new Converted(type2, ExpressionType.FLOAT);
                    default:
                        assert (type1 == type2) : inFunction + ": Cannot multiply FLOAT with " + type2;
                }
                break;
            default:
                assert false
                        : inFunction + ": Cannot multiply " + type1 + " with " + type2;
        }
        return new Converted(ExpressionType.NONE);
    }
    private Converted combineTypesDiv(String inFunction,
                                      ExpressionType typeA,
                                      ExpressionType typeB,
                                      ExpressionType typeAMinor,
                                      ExpressionType typeBMinor) {
        Boolean swappedMajor = false;
        ExpressionType type1 = typeA
                , type2 = typeB
                , type1Minor = typeAMinor
                , type2Minor = typeBMinor;
        if (typeA.ordinal() > typeB.ordinal()) {
            type1 = typeB;
            type2 = typeA;
            type1Minor = typeBMinor;
            type2Minor = typeAMinor;
            swappedMajor = true;
        }

        switch (type1) {
            case CLOCK:
                // System.err.println("Clock 1's minor type is " + type1Minor);
                assert (type1Minor == translator.timeUnit.type)
                        : (inFunction
                        + ": Clock's minor type different from timeUnit's");
                switch (type2) {
                    case CLOCK:
                        assert false
                                : (inFunction+": Prism cannot handle clock division");
                    case SECONDS:
                    case MINUTES:
                    case HOURS:
                    case DAYS:
                    case WEEKS:
                        assert false
                                : inFunction + ": We cannot handle clock/duration division."
                                + "\nPlease rewrite, e.g. c/d >= int as c >= int*d";
                    case INT:
                        return new Converted(ExpressionType.CLOCK, ExpressionType.INT);
                    default:
                        assert (type2==ExpressionType.INT)
                                : (inFunction
                                + ": Clocks can be divide by INTs only");
                        // return this;
                }
            case INT:
                switch (type2) {
                    case INT:
                        return new Converted(ExpressionType.INT);
                    case FLOAT:
                        return new Converted(ExpressionType.FLOAT);
                    case SECONDS:
                    case MINUTES:
                    case HOURS:
                    case DAYS:
                    case WEEKS:
                        assert swappedMajor
                                : (inFunction+": We cannot handle int/duration division.\nPlease rewrite.");
                        return new Converted(type2, type2Minor);
                    default:
                        assert type1 == type2 : inFunction + ": Cannot divide INT by " + type2;
                }
                break;
            case FLOAT:
                switch (type2) {
                    case INT:
                        return new Converted(ExpressionType.FLOAT);
                    case FLOAT:
                        return new Converted(ExpressionType.FLOAT);
                    case SECONDS:
                    case MINUTES:
                    case HOURS:
                    case DAYS:
                    case WEEKS:
                        /*
                         * Duration expressions should be INT but one can then
                         * floor/ceil it, so we should accept multiplication
                         * by a float.
                         */
                        assert swappedMajor
                                : (inFunction+": We cannot handle float/duration division.\nPlease rewrite.");
                        return new Converted(type2, ExpressionType.FLOAT);
                    default:
                        assert (type1 == type2) : inFunction + ": Cannot divide FLOAT by " + type2;
                }
                break;
            case SECONDS:
            case MINUTES:
            case HOURS:
            case DAYS:
            case WEEKS:
                switch (type2) {
                    case SECONDS:
                    case MINUTES:
                    case HOURS:
                    case DAYS:
                    case WEEKS:
                        /*
                         * Find out what their minor type is (INT/FLOAT?) and
                         * what it takes to scale one to the other when
                         * they're not using the same units, e.g.,
                         * 3.5weeks/4seconds
                         */
                        Converted plusConversion = combineTypesAdd(inFunction
                                , type1
                                , type2
                                , type1Minor
                                , type2Minor);
                        return new Converted(plusConversion.resTypeMinor
                                , plusConversion.resTypeMinor
                                , plusConversion.scale0
                                , plusConversion.scale1);
                    default:
                        assert false
                                : inFunction+": We cannot divide " + type1 + " by " + type2;
                }
            default:
                assert false
                        : inFunction + ": Cannot divide " + type1 + " by " + type2;
        }
        return new Converted(ExpressionType.NONE);
    }

    public EvalNode visitTExprNumericalAbsPlus(org.cumulus.certificate.model.Plus type) {
        List<JAXBElement<?>> args = type.getExprNumericalAbs();
        assert ( 0 < args.size() ) : "Plus requires at least one argument";
        final String op = "+";
        EvalNode first = visit(args.get(0).getValue());
        String toPrism = first.toPrism;
        String toLisp = first.toLisp;
        ExpressionType resType = first.type;
        ExpressionType resTypeMinor = first.typeMinor;
        for (int i = 1; i < args.size(); ++i) {
            EvalNode argi = visit(args.get(i).getValue());
            ExpressionType argiType = argi.type;
            ExpressionType argiTypeMinor = argi.typeMinor;
            Converted conversion = combineTypesAdd("Plus",
                    resType,
                    argiType,
                    resTypeMinor,
                    argiTypeMinor);
            if (conversion.scale0 != 1) { // protect op before multiplying
                toPrism = "(" + toPrism + ")";
                toLisp = "(" + op + " " + toLisp + ")";
            }
            toPrism = conversion.toPrism0(toPrism)
                    + " " + op + " "
                    + conversion.toPrism1(argi.toPrism);
            resType = conversion.resType;
            resTypeMinor = conversion.resTypeMinor;
            toLisp = Converted.scaleToLisp(toLisp, conversion.scale0, resType, argiType)
                    + " "
                    + Converted.scaleToLisp(argi.toLisp, conversion.scale1, argiType, resType);
        }
        return new EvalNode(((1 == args.size())
                ? "(0 " + op + " "
                : "(")
                + toPrism + ")"
                , "(" + op + " " + toLisp + ")"
                , resType
                , resTypeMinor);
    }

    public EvalNode visitTExprNumericalAbsMinus(org.cumulus.certificate.model.Minus type) {
        List<JAXBElement<?>> args = type.getExprNumericalAbs();
        assert ( 0 < args.size() ) : "Minus requires at least one argument";
        final String op = "-";
        EvalNode first = visit(args.get(0).getValue());
        String toPrism = first.toPrism;
        String toLisp = first.toLisp;
        ExpressionType resType = first.type;
        ExpressionType resTypeMinor = first.typeMinor;
        for (int i = 1; i < args.size(); ++i) {
            EvalNode argi = visit(args.get(i).getValue());
            ExpressionType argiType = argi.type;
            ExpressionType argiTypeMinor = argi.typeMinor;
            Converted conversion = combineTypesAdd("Plus",
                    resType,
                    argiType,
                    resTypeMinor,
                    argiTypeMinor);
            if (conversion.scale0 != 1) { // protect op before multiplying
                toPrism = "(" + toPrism + ")";
                toLisp = "(" + op + " " + toLisp + ")";
            }
            toPrism = conversion.toPrism0(toPrism)
                    + " " + op + " "
                    + conversion.toPrism1(argi.toPrism);
            resType = conversion.resType;
            resTypeMinor = conversion.resTypeMinor;
            toLisp = Converted.scaleToLisp(toLisp, conversion.scale0, resType, argiType)
                    + " "
                    + Converted.scaleToLisp(argi.toLisp, conversion.scale1, argiType, resType);
        }
        return new EvalNode(((1 == args.size())
                ? "(0 " + op + " "
                : "(")
                + toPrism + ")"
                , "(" + op + " " + toLisp + ")"
                , resType
                , resTypeMinor);
    }

    public EvalNode visitTExprNumericalAbsMultiply(org.cumulus.certificate.model.Multiply type) {
        List<JAXBElement<?>> args = type.getExprNumericalAbs();
        assert ( 0 < args.size() ) : "Multiply requires at least one argument";
        final String op = "*";
        EvalNode first = visit(args.get(0).getValue());
        String toPrism = first.toPrism;
        String toLisp = first.toLisp;
        ExpressionType resType = first.type;
        ExpressionType resTypeMinor = first.typeMinor;
        for (int i = 1; i < args.size(); ++i) {
            EvalNode argi = visit(args.get(i).getValue());
            ExpressionType argiType = argi.type;
            ExpressionType argiTypeMinor = argi.typeMinor;
            Converted conversion = combineTypesMult("Multiply",
                    resType,
                    argiType,
                    resTypeMinor,
                    argiTypeMinor);
            // if (conversion.scale0 != 1) { // protect op before multiplying
            //	toPrism = "(" + toPrism + ")";
            //	toLisp = "(" + op + " " + toLisp + ")";
            // }
            toPrism = conversion.toPrism0(toPrism)
                    + " " + op + " "
                    + conversion.toPrism1(argi.toPrism);
            resType = conversion.resType;
            resTypeMinor = conversion.resTypeMinor;
            toLisp = Converted.scaleToLisp(toLisp, conversion.scale0, resType, argiType)
                    + " "
                    + Converted.scaleToLisp(argi.toLisp, conversion.scale1, argiType, resType);
        }
        return new EvalNode(((1 == args.size())
                ? "(1 " + op + " "
                : "(")
                + toPrism + ")"
                , "(" + op + " " + toLisp + ")"
                , resType
                , resTypeMinor);
    }

    public EvalNode visitTExprNumericalAbsDivide(org.cumulus.certificate.model.Divide type) {
        List<JAXBElement<?>> args = type.getExprNumericalAbs();
        assert ( 0 < args.size() ) : "Divide requires at least one argument";
        final String op = "/";
        EvalNode first = visit(args.get(0).getValue());
        String toPrism = first.toPrism;
        String toLisp = first.toLisp;
        ExpressionType resType = first.type;
        ExpressionType resTypeMinor = first.typeMinor;
        for (int i = 1; i < args.size(); ++i) {
            EvalNode argi = visit(args.get(i).getValue());
            ExpressionType argiType = argi.type;
            ExpressionType argiTypeMinor = argi.typeMinor;
            Converted conversion = combineTypesDiv("Divide",
                    resType,
                    argiType,
                    resTypeMinor,
                    argiTypeMinor);
            // if (conversion.scale0 != 1) { // protect op before multiplying
            //	toPrism = "(" + toPrism + ")";
            //	toLisp = "(" + op + " " + toLisp + ")";
            // }
            toPrism = conversion.toPrism0(toPrism)
                    + " " + op + " "
                    + conversion.toPrism1(argi.toPrism);
            resType = conversion.resType;
            resTypeMinor = conversion.resTypeMinor;
            toLisp = Converted.scaleToLisp(toLisp, conversion.scale0, resType, argiType)
                    + " "
                    + Converted.scaleToLisp(argi.toLisp, conversion.scale1, argiType, resType);
        }
        return new EvalNode(((resType == ExpressionType.INT) // an INT result
                ? "floor("
                : "(")
                +
                ((1 == args.size())
                        ? "1 " + op + " "
                        : "")
                + toPrism + ")"
                , "(" + op + " " + toLisp + ")"
                , resType
                , resTypeMinor);
    }

    public EvalNode visitTExprNumericalAbsITEn(org.cumulus.certificate.model.ITEn type) {
        TExprBooleanAbs predArg = type.getExprBooleanAbs().getValue();

        List<JAXBElement<?>> args = type.getExprNumericalAbs();
        // assert ( 2 == args.size() ) : "ITEn requires at least one argument";
        final String op = "+";
        EvalNode pred = visit(predArg);
        EvalNode first = visit(args.get(0).getValue());
        EvalNode second = visit(args.get(1).getValue());
        String toPrism = "(" + pred.toPrism
                + " ? " + first.toPrism
                + " : " + second.toPrism + ")" ;

        ExpressionType resType = first.type;
        ExpressionType resTypeMinor = first.typeMinor;
        // for (int i = 1; i < args.size(); ++i) {
        //     EvalNode argi = visit(args.get(i).getValue());
        //     ExpressionType argiType = argi.type;
        //     ExpressionType argiTypeMinor = argi.typeMinor;
        //     Converted conversion = combineTypesAdd("Plus",
        //					      resType,
        //					      argiType,
        //					      resTypeMinor,
        //					      argiTypeMinor);
        //     if (conversion.scale0 != 1) { // protect op before multiplying
        //	toPrism = "(" + toPrism + ")";
        //     }
        //     toPrism = conversion.toPrism0(toPrism)
        //	   + " " + op + " "
        //	   + conversion.toPrism1(argi.toPrism);
        //     resType = conversion.resType;
        //     resTypeMinor = conversion.resTypeMinor;
        //     /*toLisp = Converted.scaleToLisp(toLisp, conversion.scale0, resType, argiType)
        //	+ " "
        //	+ Converted.scaleToLisp(argi.toLisp, conversion.scale1, argiType, resType);*/
        // }
        return new EvalNode(toPrism
                , "(if " + pred.toLisp
                + " " + first.toLisp
                + " " + second.toLisp + ")"
                , resType
                , resTypeMinor);
    }
    //
    public EvalNode visitTExprNumericalAbsViolatedAssertions(org.cumulus.certificate.model.ViolatedAssertions type) {
        String fname = getClassName(type);
        ExpressionType typeInfo = ((IDInfo) translator.idTypeMap.get(fname)).type;
        return new EvalNode(fname
                , "(***ViolatedAssertions***)"
                , typeInfo);
    }


    public EvalNode visitTExprNumericalAbsViolations(org.cumulus.certificate.model.Violations type) {
        String fname = getClassName(type);
        ExpressionType typeInfo = ((IDInfo) translator.idTypeMap.get(fname)).type;
        String gtName = type.getId().getName();
        // Note the name of the GT, to check afterwards that it has
        // been declared.
        translator.usedGT(gtName);
        return new EvalNode(// fname,
                (((translator.GLOB_Current_GTName != null) && translator.GLOB_Current_GTName.equals(gtName))
                        ? "INC" : "")
                        + "violations_" + gtName
                , "*violations-"
                + gtName + "*"
                , typeInfo);
    }

    public EvalNode visitTExprNumericalAbsPenaltyAmount(org.cumulus.certificate.model.PenaltyAmount type) {
        String fname = getClassName(type);
        ExpressionType typeInfo = ExpressionType.INT; //((IDInfo) translator.idTypeMap.get(fname)).type;
        String gtName = type.getId().getName();
        // Note the name of the GT, to check afterwards that it has
        // been declared.
        translator.usedGT(gtName);
        return new EvalNode(// fname,
                "penalty_amount_" + gtName
                , "(***PenaltyAmount*** "
                + gtName + ")"
                , typeInfo);
    }

    public EvalNode visitTExprNumericalAbsCounter(org.cumulus.certificate.model.Counter type) {
        String fname = getClassName(type);
        ExpressionType typeInfo = ExpressionType.INT; //((IDInfo) translator.idTypeMap.get(fname)).type;
        String actionName = type.getIds().get(0).getName();
        String gtName = type.getIds().get(1).getName();
        // Note the names of the GT & the (action,GT) pair, to check
        // afterwards that they have been declared.
        translator.usedGT(gtName);
        translator.usedActionGT(actionName, gtName);
        return new EvalNode(// fname,
                "counter_"
                        + actionName
                        + "_"
                        + gtName
                , "(***Counter*** "
                + actionName
                + " "
                + gtName
                + ")"
                , typeInfo);
    }

    public EvalNode visitTExprNumericalAbsUnresolvedAnomalies(org.cumulus.certificate.model.UnresolvedAnomalies type) {
        String fname = getClassName(type);
        ExpressionType typeInfo = ((IDInfo) translator.idTypeMap.get(fname)).type;
        return new EvalNode(fname, "(***UnresolvedAnomalies***)", typeInfo);
    }
    public EvalNode visitTExprNumericalAbsResolvedAnomalies(org.cumulus.certificate.model.ResolvedAnomalies type) {
        String fname = getClassName(type);
        ExpressionType typeInfo = ((IDInfo) translator.idTypeMap.get(fname)).type;
        return new EvalNode(fname, "(***ResolvedAnomalies***)", typeInfo);
    }
    public EvalNode visitTExprNumericalAbsDetectedAnomalies(org.cumulus.certificate.model.DetectedAnomalies type) {
        String fname = getClassName(type);
        ExpressionType typeInfo = ((IDInfo) translator.idTypeMap.get(fname)).type;
        return new EvalNode(fname, "(***DetectedAnomalies***)", typeInfo);
    }

    public EvalNode visitTExprNumericalAbsSeenEvents(org.cumulus.certificate.model.SeenEvents type) {
        String fname = getClassName(type);
        ExpressionType typeInfo = ((IDInfo) translator.idTypeMap.get(fname)).type;
        return new EvalNode(fname, "(***SeenEvents***)", typeInfo);
    }

    public EvalNode visitTermTypeGuaranteeTermType(org.ggf.schemas.graap._2007._03.ws_agreement.GuaranteeTermType type){
        String fname = getClassName(type);
        IDInfo info = translator.idTypeMap.get(fname); // no GuaranteeTermType in idTypeMap ---- NULL!!!!!!!
        ExpressionType typeInfo = info.type;
        return new EvalNode(fname, "", typeInfo);
    }

    public EvalNode visitGuardedActionType(org.ggf.schemas.graap._2007._03.ws_agreement.GuardedActionType type){
        String fname = getClassName(type);
        IDInfo info = translator.idTypeMap.get(fname); // no GuaranteeTermType in idTypeMap ---- NULL!!!!!!!
        ExpressionType typeInfo = info.type;
        return new EvalNode(fname, "", typeInfo);
    }

    public EvalNode visitCustomBusinessValueType(org.ggf.schemas.graap._2007._03.ws_agreement.CustomBusinessValueType type){
        String fname = getClassName(type);
        IDInfo info = translator.idTypeMap.get(fname); // no GuaranteeTermType in idTypeMap ---- NULL!!!!!!!
        ExpressionType typeInfo = info.type;
        return new EvalNode(fname, "", typeInfo);
    }

    //
    public EvalNode visit(Object o) {
        if (o == null) throw new RuntimeException("Null argument to visit");

        String parentClassName = o.getClass().getSuperclass().getName();
        // Class.getName() returns package information as well.	 This
        // strips off the package information giving us just the class
        // name
        parentClassName = parentClassName
                .substring(parentClassName.lastIndexOf('.')+1);

        String className = getClassName(o);
        String methodName = "visit"+ parentClassName + className;
        EvalNode res = null;
        // Now we try to invoke the method visit<parentClassName><className>
        Method m = null;
        try {
            // Get the method visit<parentClassName><className>(<className> foo)
            m = getClass().getMethod(methodName,
                    new Class[] { o.getClass() });
        } catch (NoSuchMethodException nsme) {
            // No method, so do the default implementation
            methodName = "visit" + parentClassName + "Default";
            try {
                m = getClass().getMethod(methodName,
                        new Class[] { o.getClass() });
                // Try to invoke visit<parentClassName>Default(Object foo)
            } catch (NoSuchMethodException nsme2) {
                // Cannot resolve this - something's bad.
                throw new AssertionError("Reflective invoke of default failed for " + methodName + " for parent=" + parentClassName + " class=" + className, nsme2);
            }
        }
        // Try to invoke visit<parentClassName><className>(<className> foo)
        try {
            res = (EvalNode) m.invoke(this, new Object[] { o });
        } catch (IllegalAccessException iae) {
            iae.printStackTrace();
            throw new AssertionError("Reflective invoke failed for " + methodName + " for parent=" + parentClassName + " class=" + className + "\n\tException: " + iae);
        } catch (InvocationTargetException ite) {
            ite.printStackTrace();
            throw new AssertionError("Reflective invoke failed for " + methodName + " for parent=" + parentClassName + " class=" + className + "\n\tException: " + ite);
        }
        return res;
    }
}

class LispActionRec {
    public String name;
    public String guard;
    public String action;
    public java.math.BigInteger penalty;
    public LispActionRec(String n, String g, String a, java.math.BigInteger p) {
        name = n;
        guard = g;
        action = a;
        penalty = p;
    }
}
class LispGTRec {
    public String name;
    public List<LispActionRec> actions;
    public LispGTRec(String n, List<LispActionRec> a) {
        name = n;
        actions = a;
    }
}
