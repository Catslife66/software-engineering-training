# 🧩 Wishlist Feature

Feature:

- user saves a property to wishlist
- prevent duplicate saves
- return success message

Design:

- Controller
- Service
- Repository
- Domain classes
- Any interfaces if needed

👉 Focus on:

- layering
- SRP
- where logic should go

```
User
|-- class User
|-- interface UserRepository
|   method: findById(String userId)

Property
|-- class Property
|-- interface PropertyRepository
|   method: findById(String propertyId)

Wishlist
|-- class WishlistItem (fields: id, userId, propertyId, createdAt)
|-- interface WishlistRepository
|   methods:
|   - findByUserAndProperty(String userId, String propertyId)
|   - save(WishlistItem wishlistItem)
|
|-- class WishlistController ('/users/{userId}/wishlist/{propertyId}')
|                         or ('/wishlist') with request body containing userId and propertyId
|   method:
|   - add(String userId, String propertyId)
|
|-- class WishlistService
|   fields:
|   - UserRepository userRepository
|   - PropertyRepository propertyRepository
|   - WishlistRepository wishlistRepository
|   method:
|   - String addToWishlist(String userId, String propertyId)
|       User user = userRepository.findById(userId)
|       if (user == null) throw new NotFoundException("User not found");
|       Property property = propertyRepository.findById(propertyId)
|       if (property == null) throw new NotFoundException("Property not found");
|       WishlistItem existing = wishlistRepository.findByUserAndProperty(userId, propertyId)
|       if (existing != null)
|           return "Property already saved."
|       WishlistItem item = new WishlistItem(userId, propertyId)
|       wishlistRepository.save(item)
|       return "Property saved to wishlist."

```
