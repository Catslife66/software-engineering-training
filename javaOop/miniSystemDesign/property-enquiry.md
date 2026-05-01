# Property Enquiry

**Scenario**

A user is browsing a property and sends an enquiry to the agent.

Endpoint:

```
POST /properties/{propertyId}/enquiries
```

Request body:

```
{
  "name": "Alice",
  "email": "alice@example.com",
  "message": "I would like to book a viewing."
}
```

**Requirements**

The system should:

- check the property exists
- validate enquiry input
- create an enquiry record
- save it
- notify the agent
- return success/failure

Controller:

- receives propertyId from path
- receives EnquiryRequestDTO from body
- calls enquiryService.createEnquiry(propertyId, request)
- maps Result to HTTP response

Service:

- validate name/email/message
- find property by propertyId
- if property missing -> Result failure PROPERTY_NOT_FOUND
- find agent by property.agentId
- if agent missing -> Result failure AGENT_NOT_FOUND
- create Enquiry
- save Enquiry
- call NotificationSender to notify agent
- return Result success

Repositories:

- PropertyRepository.findById(propertyId)
- UserRepository.findById(agentId)
- EnquiryRepository.save(enquiry)

Helper services:

- NotificationSender.send(toEmail, message)

DTOs:

- EnquiryRequestDTO {name, email, message}

Models:

- Property (id, name, address, agentId)
- Enquiry (id, propertyId, enquiryFromName, enquiryFromEmail, message)
- User (id, name, email, role)

Flow:

- client sends property enquiry request
- system validates requestBody and check whether property exists
- system saves the enquiry and notify the property agent
- client receives a response

Possible Results:

- invalid request body -> 400 INVALID_INPUT
- property does not exist -> 404 PROPERTY_NOT_FOUND
- agent not found -> 404 AGENT_NOT_FOUND or 500 depending on design
- success -> 201 CREATED
- unexpected DB/email failure -> exception / 500
