# Implement JPA Relationship to Property

Business Model

```
Agent 1 ──────< many Properties
Property 1 ───< many Viewings
Viewing many ───> 1 Property
```

Agent ↔ Property

```
Agent
   ▲
   │ @OneToMany
   │
Property
   │
   ▼ @ManyToOne
```

Foreign key:

```
properties.agent_id
```

Owning side:

```
Property
```

Property ↔ Viewing

```
Property
    ▲
    │ @OneToMany
    │
Viewing
    │
    ▼ @ManyToOne
```

Foreign key:

```
viewings.property_id
```

Owning side:

```
Viewing
```

## Create Agent Entity

```
@Entity
@Table(name = "agents")
public class Agent {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @OneToMany(mappedBy = "agent")
    private List<Property> properties;

    protected Agent(){
    }

    public Agent(String name, String email){
        this.id = UUID.randomUUID();
        this.name = name;
        this.email = email;
    }

    void addProperty(Property property){
        properties.add(property);
        property.setAgent(this);
    }

    void removeProperty(Property property){
        properties.remove(property);
        properties.setAgent(null);
    }

    public UUID getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public String getEmail(){
        return email;
    }

    public List<Property> getProperties(){
        return properties;
    }

}
```

## Update Property Entity

```
@Entity
@Table(name = "properties")
public class Property {

    ...

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    private Agent agent;

    @OneToMany(
        mappedBy = "properties",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<Viewing> viewings;

    ...

    void addViewing(Viewing viewing){
        viewings.add(viewing);
        viewings.setProperty(this);
    }

    void removeViewing(Viewing viewing){
        viewings.remove(viewing);
        viewings.setProperty(null);
    }

    void setAgent(Agent agent){
        this.agent = agent;
    }

    public Agent getAgent(){
        return agent;
    }

    public List<Viewing> getViewings(){
        return viewings;
    }
}
```

## Create Viewing Entity

```
@Entity
@Table(name = "viewings")
public class Viewing{
    @Id
    private UUID id;

    @Column(nullable = false)
    private LocalDateTime requestedDateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ViewingStatus status = ViewingStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    protected Viewing(){}

    public Viewing(LocalDateTime requestedDateTime){
        this.id = UUID.randomUUID();
        this.requestedDateTime = requestedDateTime;
    }

    // To force everyone to use property.addViewing(...) instead
    void setProperty(Property property){
        this.property = property;
    }

    public UUID getId(){
        return id;
    }

    public Property getProperty(){
        return property;
    }

    public LocalDateTime getRequestedDateTime(){
        return requestedDateTime;
    }

    public ViewingStatus getStatus(){
        return status;
    }
}

public enum ViewingStatus{
    PENDING,
    APPROVED,
    REJECTED,
    CANCELLED
}
```

## Relationship picture

```
Agent
  @OneToMany(mappedBy = "agent")
        ↓
Property
  @ManyToOne + agent_id

Property
  @OneToMany(mappedBy = "property",
             cascade = ALL,
             orphanRemoval = true)
        ↓
Viewing
  @ManyToOne + property_id
```
