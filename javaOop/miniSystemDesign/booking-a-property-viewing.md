# Booking a Property Viewing

**Scenario**

A user wants to book a viewing for a property.

Endpoint:

```
POST /properties/{propertyId}/viewings
```

Request body:

```
{
  "userId": "u123",
  "requestedDateTime": "2026-06-10T14:00:00"
}
```

**Requirements**

The system should:

- validate request input
- check the user exists
- check the property exists
- check the requested time is available
- create a viewing booking
- save it
- notify the agent and user
- return success/failure

Controller:

- receives propertyId from @PathVariable
- receives ViewingRequestDTO from @RequestBody
- calls viewingService.createViewing(propertyId, request)
- maps Result to HTTP response

Service:

- validate userId and requestedDateTime
  - reject requestedDateTime if it is in the past
- find user by userId
  - if missing -> USER_NOT_FOUND
- find property by propertyId
  - if missing -> PROPERTY_NOT_FOUND
- check existing booking for property/dateTime
  - if booked -> BOOKING_CONFLICT
- find agent by property.agentId
- create Viewing
- save Viewing
- notify user
- notify agent
- return success

Repositories:

- UserRepository.findById(userId)
- UserRepository.findById(agentId)
- PropertyRepository.findById(propertyId)
- ViewingRepository.findByPropertyAndDateTime(propertyId, requestedDateTime)
- ViewingRepository.save(viewing)

Helper services:

- NotificationService.send(toEmail, message)
- optionally ViewingAvailabilityService.isAvailable(propertyId, requestedDateTime)

DTO:

- ViewingRequestDTO { userId, requestedDateTime }

Models:

- User(id, name, email, role)
- Property(id, name, address, agentId)
- Viewing(id, propertyId, userId, requestedDateTime, status)

Results:

- INVALID_INPUT -> 400
- USER_NOT_FOUND -> 404
- PROPERTY_NOT_FOUND -> 404
- BOOKING_CONFLICT -> 409
- SUCCESS -> 201
- unexpected error -> 500
