
# Mule Anypoint Template: SFDC to SFDC Contact aggregation

+ [Use Case](#usecase)
+ [Run it!](#runit)
    * [Running on CloudHub](#runoncloudhub)
       	* [Deploying your Template on CloudHub](#deployingyourtemplateoncloudhub)
    * [Running on premise](#runonopremise)
        * [Properties to be configured](#propertiestobeconfigured)
+ [Customize It!](#customizeit)
    * [config.xml](#configxml)
    * [endpoints.xml](#endpointsxml)
    * [businessLogic.xml](#businesslogicxml)
    * [errorHandling.xml](#errorhandlingxml)
+ [Testing the Template](#testingthetemplate)
    

# Use Case <a name="usecase"/>
As a Salesforce admin I want to aggregate contacts from two Salesforce Instances and compare them to see which contacts can only be found in one of the two and which contacts are in both instances. 

For practical purposes this Template will generate the result in the format of a CSV Report sent by mail.

This Template should serve as a foundation for extracting data from two systems, aggregating data, comparing values of fields for the objects, and generating a report on the differences. 

As implemented, it gets contacts from two instances of Salesforce, compares by the email address of the contacts, and generates a CSV file which shows contact in A, contact in B, and contacts in A and B. The report is then emailed to a configured group of email addresses.

# Run it! <a name="runit"/>

Simple steps to get SFDC to SFDC Contact Aggregation running 

## Running on CloudHub <a name="runoncloudhub"/>

While [creating your application on CloudHub](http://www.mulesoft.org/documentation/display/current/Hello+World+on+CloudHub) (Or you can do it later as a next step), you need to go to Deployment > Advanced to set all environment variables detailed in **Properties to be configured** as well as the **mule.env**. 

Once your app is all set and started, supposing you choose as domain name `sfdccontactaggregation` to trigger the use case you just need to hit `http://sfdccontactaggregation.cloudhub.io/generatereport` and the report will be sent to the emails configured.

### Deploying your Template on CloudHub <a name="deployingyourtemplateoncloudhub"/>
Mule Studio provides you with really easy way to deploy your Template directly to CloudHub, for the specific steps to do so please check this [link](http://www.mulesoft.org/documentation/display/current/Deploying+Mule+Applications#DeployingMuleApplications-DeploytoCloudHub)


## Running on premise <a name="runonopremise"/>
Complete all properties in one of the property files, for example in [mule.prod.properties] (../blob/master/src/main/resources/mule.prod.properties) and run your app with the corresponding environment variable to use it. To follow the example, this will be `mule.env=prod`.

After this, to trigger the use case you just need to hit the local http endpoint with the port you configured in your file. If this is, for instance, `9090` then you should hit: `http://localhost:9090/generatereport` and this will create a CSV report and send it to the mails set.

## Properties to be configured (With examples)<a name="propertiestobeconfigured"/>

In order to use this Mule Template you need to configure properties (Credentials, configurations, etc.) either in properties file or in CloudHub as Environment Variables. Detail list with examples:

### Application configuration
+ http.port `9090` 

#### SalesForce Connector configuration for company A
+ sfdc.a.username `bob.dylan@orga`
+ sfdc.a.password `DylanPassword123`
+ sfdc.a.securityToken `avsfwCUl7apQs56Xq2AKi3X`
+ sfdc.a.url `https://login.salesforce.com/services/Soap/u/26.0`

#### SalesForce Connector configuration for company B
+ sfdc.b.username `joan.baez@orgb`
+ sfdc.b.password `JoanBaez456`
+ sfdc.b.securityToken `ces56arl7apQs56XTddf34X`
+ sfdc.b.url `https://login.salesforce.com/services/Soap/u/26.0`


#### SMPT Services configuration
+ smtp.host `smtp.gmail.com`
+ smtp.port `587`
+ smtp.user `exampleuser@gmail.com`
+ smtp.password `ExamplePassword456`

#### Mail details
+ mail.from `exampleuser@gmail.com`
+ mail.to `woody.guthrie@gmail.com`
+ mail.subject `SFDC Users Report`
+ mail.body `Contacts report comparing contacts from SFDC Accounts`
+ attachment.name `OrderedReport.csv`


# Customize It!<a name="customizeit"/>

This brief guide intends to give a high level idea of how this Template is built and how you can change it according to your needs.
As mule applications are based on XML files, this page will be organized by describing all the XML that conform the Template.
Of course more files will be found such as Test Classes and [Mule Application Files](http://www.mulesoft.org/documentation/display/current/Application+Format), but to keep it simple we will focus on the XMLs.

Here is a list of the main XML files you'll find in this application:

* [config.xml](#configxml)
* [endpoints.xml](#endpointsxml)
* [businessLogic.xml](#businesslogicxml)
* [errorHandling.xml](#errorhandlingxml)


## config.xml<a name="configxml"/>
Configuration for Connectors and [Properties Place Holders](http://www.mulesoft.org/documentation/display/current/Configuring+Properties) are set in this file. **Even you can change the configuration here, all parameters that can be modified here are in properties file, and this is the recommended place to do it so.** Of course if you want to do core changes to the logic you will probably need to modify this file.

In the visual editor they can be found on the *Global Element* tab.


## endpoints.xml<a name="endpointsxml"/>
This is the file where you will found the inbound and outbound sides of your integration app.
This Template has an [HTTP Inbound Endpoint](http://www.mulesoft.org/documentation/display/current/HTTP+Endpoint+Reference) as the way to trigger the use case and an [SMTP Transport](http://www.mulesoft.org/documentation/display/current/SMTP+Transport+Reference) as the outbound way to send the report.

###  Trigger Flow
**HTTP Inbound Endpoint** - Start Report Generation
+ `${http.port}` is set as a property to be defined either on a property file or in CloudHub environment variables.
+ The path configured by default is `generatereport` and you are free to change for the one you prefer.
+ The host name for all endpoints in your CloudHub configuration should be defined as `localhost`. CloudHub will then route requests from your application domain URL to the endpoint.

###  Outbound Flow
**SMTP Outbound Endpoint** - Send Mail
+ Both SMTP Server configuration and the actual mail to be sent are defined in this endpoint.
+ This flow is going to be invoked from the flow that does all the functional work: *mainFlow*, the same that is invoked from the Inbound Flow upon triggering of the HTTP Endpoint.


## businessLogic.xml<a name="businesslogicxml"/>
Functional aspect of the Template is implemented on this XML, directed by one flow responsible of conducting the aggregation of data, comparing records and finally formating the output, in this case being a report.
The *mainFlow* organises the job in three different steps and finally invokes the *outboundFlow* that will deliver the report to the corresponding outbound endpoint.
This flow has Exception Strategy that basically consists on invoking the *defaultChoiseExceptionStrategy* defined in *errorHandling.xml* file.


###  Gather Data Flow
Mainly consisting of two calls (Queries) to SalesForce and storing each response on the Invocation Variable named *contactsFromOrgA* or *contactsFromOrgA* accordingly.

###  Aggregation Flow
[Java Transformer](http://www.mulesoft.org/documentation/display/current/Java+Transformer+Reference) responsible for aggregating the results from the two SalesForce Org Contacts.
Criteria and format applied:
+ Transformer receives a Mule Message with the two Invocation variables *contactsFromOrgA* and *contactsFromOrgB* to result in List of Maps with keys: **Name**, **Email**, **IDInA** and **IDInB**.
+ Contacts will be matched by mail, that is to say, a record in both SFDC organisations with same mail is considered the same contact.

###  Format Output Flow
+ [Java Transformer](http://www.mulesoft.org/documentation/display/current/Java+Transformer+Reference) responsible for sorting the list of contacts in the following order:

1. Contacts only in Org A
2. Contacts only in Org B
3. Contacts in both Org A and Org B

All records ordered alphabetically by mail within each category.
If you want to change this order then the *compare* method should be modified.

+ CSV Report [DataMapper](http://www.mulesoft.org/documentation/display/current/Datamapper+User+Guide+and+Reference) transforming the List of Maps in CSV with headers **Name**, **Email**, **IDInA**, and **IDInB**.
+ An [Object to string transformer](http://www.mulesoft.org/documentation/display/current/Transformers) is used to set the payload as an String. 


## errorHandling.xml<a name="errorhandlingxml"/>
Contains a [Catch Exception Strategy](http://www.mulesoft.org/documentation/display/current/Catch+Exception+Strategy) that is only Logging the exception thrown (If so). As you imagine, this is the right place to handle how your integration will react depending on the different exceptions. 

## Testing the Template <a name="testingthetemplate"/>

You will notice that the Template has been shipped with test.
These devidi them self into two categories:

+ Unit Tests
+ Integration Tests

You can run any of them by just doing right click on the class and clicking on run as Junit test.

Do bear in mind that you'll have to tell the test classes which property file to use.
For you convinience we have added a file mule.test.properties located in "src/test/resources".
In the run configurations of the test just make sure to add the following property:

+ -Dmule.env=test
