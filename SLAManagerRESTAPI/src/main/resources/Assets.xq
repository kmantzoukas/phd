declare namespace functx = "http://www.functx.com";
declare function functx:trim
  ( $arg as xs:string? )  as xs:string {

   replace(replace($arg,'\s+$',''),'^\s+','')
 } ;
 
declare variable $theinput as xs:string external;
declare variable $thedirectory as xs:string external;
 (: let $doc := doc('file:///Users/sbkg342/Documents/workspace/AssetList/OWL Examples/DataCleaningWhile.owls') :)
let $theMainDocumentName := fn:concat($thedirectory, '/', $theinput)
let $doc := doc($theMainDocumentName)
let $compositeService := fn:data($doc//*:Service/@*:ID)
return (fn:string-join(('{
 "compositeService" : {
   "name" : "', $compositeService, '"
   , "atomicServices" : ['), ''), (

(: Nested call code :)
 (: Read all the Groundings of services - should be just one, for the composite service. :)
 let $element := $doc/*:RDF/*:WsdlGrounding
 (: For each grounded operation that the compositeService calls do this: :)
 (: 1 - Read the node of the atomic process grounding :)
 for $atomicProcessGroundingNode in fn:data($element//*:hasAtomicProcessGrounding/@*:resource)
   (: 2 - Get the tag of it (some *random* identifier - everything after # is not "data") :)
   let $atomicProcessGroundingResource :=  fn:substring-after($atomicProcessGroundingNode, '#')
   (: 3 - Use the tag to get the full URL to the file where the service is defined :)
   (: for $serviceUsedInGroundingFile in (fn:substring-before($atomicProcessGroundingResource, 'AtomicProcessGrounding')) :)
   (: 4 - Tokenise (split) the URL using the slashes as separators and get the last part of it, i.e., the file name of the service definition :)
   let $serviceUsedInGroundingFile := fn:tokenize(fn:substring-before($atomicProcessGroundingNode, '#'), "/")[fn:last()]
   (: 5 - If the file name is empty, use the current document to search for the service definition, otherwise use the main directory and the file as the full path :)
   let $serviceUsedInGroundingDocument := doc(
     if (fn:string-length($serviceUsedInGroundingFile) = 0) then
       $theMainDocumentName
     else
       fn:concat($thedirectory , '/', $serviceUsedInGroundingFile)
   )
   let $serviceWsdlGroundingRef := fn:substring-after($serviceUsedInGroundingDocument/*:RDF/*:WsdlGrounding/@*:about[//*:WsdlAtomicProcessGrounding/@*:ID = $atomicProcessGroundingResource], '#')
   let $serviceName := $serviceUsedInGroundingDocument/*:RDF/*:Service/@*:ID[//*:WsdlGrounding/@*:ID=$serviceWsdlGroundingRef]
     
(:
   Service -> N Ports

   Port -> N Operations

   "OWL-S allows for the description of a Web service in terms of a
   *Profile*, which tells "what the service does", a *Process Model*,
   which tells "how the service works", and a *Grounding*, which tells
   "how to access the service". The Profile and Process Model are
   considered to be *abstract* specifications, in the sense that they
   do not specify the details of particular message formats,
   protocols, and network addresses by which a Web service is
   instantiated. The role of the grounding is to provide these more
   concrete details."
   [http://www.daml.org/services/owl-s/1.0/owl-s-wsdl.html]

:)
 return (fn:string-join(('
      { "serviceName" : "', $serviceName, '", "operations" : ['), ''),
   for $operationAtomicProcessGroundingRef in $serviceUsedInGroundingDocument/*:RDF/*:WsdlGrounding[@*:about=fn:string-join(('#', $serviceWsdlGroundingRef), '')]/*:hasAtomicProcessGrounding/*:WsdlAtomicProcessGrounding/[@*:ID]
     let $operationAtomicProcessGroundingNode := $serviceUsedInGroundingDocument/*:RDF/*:WsdlAtomicProcessGrounding[@*:about=fn:string-join(('#', $operationAtomicProcessGroundingRef), '')]
     let $operation := fn:substring-after($operationAtomicProcessGroundingNode//*:operation, '#')
     let $operationOutputMessageName := fn:substring-after($operationAtomicProcessGroundingNode//*:wsdlOutputMessage, '#')
     let $operationOutputParameterName := fn:substring-after($operationAtomicProcessGroundingNode//*:wsdlOutput//*:WsdlOutputMessageMap//*:wsdlMessagePart, '#')
     let $operationOutputParameterTypeNode := $serviceUsedInGroundingDocument/*:RDF/*:DatatypeProperty[*:domain/@*:resource = string-join(('#',$operationOutputParameterName), '')]
     let $operationOutputParameterType := $operationOutputParameterTypeNode/*:range/@*:resource
     
     
     let $operationInputMessageName := fn:substring-after($operationAtomicProcessGroundingNode//*:wsdlInputMessage, '#')
     
(:
  Cannot make this work... :-(

  Worse still, SparkKmean.owl has no DatatypeProperty parts, only SparkClean.owl does!
     let $operationOutputParameterTypeNode := $serviceUsedInGroundingDocument/*:RDF/*:DatatypeProperty[*:domain/@*:resource = string-join(('#',$operationOutputParameterName), '')]
     let $operationOutputParameterType := $operationOutputParameterTypeNode/*:range/@*:resource

  Since DatatypeProperty parts are missing from some Service files, it's also impossible to retrieve the input parameter types.
:)

     return (fn:string-join(('
        { "operationName" : "'
                            , $operation
                            , '"
        , "outputMessageName" : "'
      			    , $operationOutputMessageName
			    , '"
        , "outputName" : "'
                    , $operationOutputParameterName
                , '"
	  , "outputType" : "'
                    , $operationOutputParameterType
                , '"
	  , "inputMessageName" : "' ,
	                  $operationInputMessageName
	             , '",
	                  "inputParameters" : ['

, for $operationInputParameterName in $operationAtomicProcessGroundingNode//*:wsdlInput
  let $inputName := fn:substring-after($operationInputParameterName//*:WsdlInputMessageMap//*:wsdlMessagePart, '#')
    return( string-join((' { "inputName" : "', fn:substring-after($operationInputParameterName//*:WsdlInputMessageMap//*:wsdlMessagePart, '#'), '",',
                             '"inputType" : "'
                         
 ,`
    let $operationInputParameterType := $serviceUsedInGroundingDocument/*:RDF/*:DatatypeProperty[*:domain/@*:resource = string-join(( "#", $inputName), '')]
    let $inputType := $operationInputParameterType//*:range
 
   return(fn:data($inputType/@*:resource))
                             
                             ,'"},'), '') )

			    , ' ] },')
			   , '')

            )

, '] },')

   , '] } }'))

(:
        let $at := fn:concat($thedirectory , '/', $serviceUsedInGroundingFile)

            for $ats in fn:doc($at)
                let $ops := $ats//*:WsdlAtomicProcessGrounding//*:wsdlOperation
                let $atop := (fn:substring-after(fn:data($ats//*:WsdlOperationRef//*:operation), '#'))
                let $outmes :=(fn:substring-after(fn:data($ats//*:WsdlAtomicProcessGrounding//*:wsdlOutputMessage), '#')) 
            return ('{"servicename" :"',$serviceUsedInGroundingFile,'", "Operations" : [{' ,'"operation" :"',$atop,'","output":"',$outmes,'", "Inputs" : [', (
              for $inm in $ats//*:WsdlAtomicProcessGrounding//*:wsdlInput
                let $inmes := (fn:substring-after(fn:data($inm//*:WsdlInputMessageMap//*:wsdlMessagePart), '#'))
                let $result2 :=('{"input" : "', $inmes,'"},')
                return ($result2), '] }, ] },')), ' ], "OperationInstances": [', (
               for $proc in $doc/*:RDF/*:Perform
               let $procname := (fn:data($proc/@*:about))
               let $prcas := (fn:data($proc//*:process/@*:resource))
               let $asset := (fn:substring-after($prcas, '#')) 
               return('{"name":"', $procname, '" ,"Operation":"', $asset, '"},'),  ']'), (
          for $c in $doc//*:Service
    return('}'))))

:)

