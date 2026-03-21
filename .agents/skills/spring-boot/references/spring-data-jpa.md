# Spring Data JPA

- [Key principles](#key-principles)
- [IdentityGenerator (TSID)](#identitygenerator)
- [Value Object for Primary Key](#value-object-for-primary-key)
- [JPA Auditing](#use-jpa-auditing-support)
- [AssertUtil](#assertutil-class-to-validate-input-parameters)
- [Example entity](#example-jpa-entity-class)
- [Example repository](#example-userrepository)

## Key principles
Follow these principles when using Spring Data JPA:

- Create `BaseEntity` for audit fields(`createdAt`, `updatedAt`) and extend all entities from it
- Create a Value Object to represent the primary key and use `@EmbeddedId` annotation
- Create a **protected no-arg constructor** for JPA
- Create a **public constructor** with all required fields
- Validate state and throw exceptions for invalid inputs
- Explicitly define **table names** for all entities
- Explicitly define **column names** for all fields
- Use **enum types** for enum fields and `@Enumerated(EnumType.STRING)` annotation
- For logically related fields, create a Value Object to represent them
- When using value objects, embed them with `@Embedded` and `@AttributeOverrides`
- Add **domain methods** that operate on entity state
- Use **optimistic locking** with `@Version`
- Create repositories **only for aggregate roots**
- Use `@Query` with **JPQL** for custom queries
- Prefer **meaningful method names** over long Spring Data JPA finder methods
- Use **constructor expressions** or **Projections** for read operations
- Use **default methods** for convenience operations

## IdentityGenerator
To use TSID, add the following dependency:

```xml
<dependency>
    <groupId>io.hypersistence</groupId>
    <artifactId>hypersistence-utils-hibernate-71</artifactId>
    <version>3.14.1</version>
</dependency>
```

Use TSID to generate IDs as follows:

```java
import io.hypersistence.tsid.TSID;

public class IdGenerator {\n    private IdGenerator() {}\n\n    public static String generateString() {\n        return TSID.Factory.getTsid().toString();\n    }\n}\n```\n\n## Value Object for Primary Key\n```java\npublic record UserId(String id) {\n    public UserId {\n        if (id == null || id.trim().isBlank()) {\n            throw new IllegalArgumentException(\"User id cannot be null or empty\");\n        }\n    }\n\n    public static UserId of(String id) {\n        return new UserId(id);\n    }\n\n    public static UserId generate() {\n        return new UserId(IdGenerator.generateString());\n    }\n}\n```\n\n## Use JPA Auditing Support\n- Add `@CreatedDate` and `@LastModifiedDate` annotations to your `BaseEntity` class.\n- Add `@EntityListeners(AuditingEntityListener.class)` to your `BaseEntity` class.\n- Create a Spring `@Configuration` class and add `@EnableJpaAuditing` annotation.\n\n**File:** `BaseEntity.java`\n\n```java\nimport jakarta.persistence.Column;\nimport jakarta.persistence.EntityListeners;\nimport jakarta.persistence.MappedSuperclass;\nimport org.springframework.data.annotation.CreatedDate;\nimport org.springframework.data.annotation.LastModifiedDate;\nimport org.springframework.data.jpa.domain.support.AuditingEntityListener;\nimport java.time.Instant;\n\n@MappedSuperclass\n@EntityListeners(AuditingEntityListener.class)\npublic abstract class BaseEntity {\n\n    @Column(name = \"created_at\", nullable = false, updatable = false)\n    @CreatedDate\n    protected Instant createdAt;\n\n    @Column(name = \"updated_at\", nullable = false)\n    @LastModifiedDate\n    protected Instant updatedAt;\n    \n    @Version\n    private int version;\n    \n    public Instant getCreatedAt() {\n        return createdAt;\n    }\n\n    public void setCreatedAt(Instant createdAt) {\n        this.createdAt = createdAt;\n    }\n\n    public Instant getUpdatedAt() {\n        return updatedAt;\n    }\n\n    public void setUpdatedAt(Instant updatedAt) {\n        this.updatedAt = updatedAt;\n    }\n}\n```\n\n**Enable JPA Auditing** in your application configuration:\n\n```java\n@Configuration\n@EnableJpaAuditing\npublic class JpaConfig {\n}\n```\n\n### AssertUtil class to validate input parameters\nCreate a `AssertUtil` class with static methods to validate input parameters.\n\n```java\npublic class AssertUtil {\n    private AssertUtil() {}\n\n    public static <T> T requireNotNull(T obj, String message) {\n        if (obj == null)\n            throw new IllegalArgumentException(message);\n        return obj;\n    }\n}\n```\n\n### Example JPA Entity Class\nWhile Creating a new JPA entity class, extend it from `BaseEntity`:\n\n```java\nimport jakarta.persistence.*;\n\nimport java.time.Instant;\n\n@Entity\n@Table(name = \"users\")\nclass UserEntity extends BaseEntity {\n\n    @EmbeddedId\n    @AttributeOverride(name = \"id\", column = @Column(name = \"id\", nullable = false))\n    private UserId id;\n\n    @Embedded\n    @AttributeOverrides({\n        @AttributeOverride(name = \"addrLine1\", column = @Column(name = \"addr_line1\", nullable = false)),\n        @AttributeOverride(name = \"addrLine2\", column = @Column(name = \"addr_line2\")),\n        @AttributeOverride(name = \"city\", column = @Column(name = \"city\"))\n    })\n    private Address address;\n\n    @Enumerated(EnumType.STRING)\n    @Column(name = \"role\", nullable = false)\n    private Role role;\n\n    //.. other fields\n\n\n    // Protected constructor for JPA\n    protected UserEntity() {}\n\n    // Constructor with all required fields\n    public UserEntity(UserId id,\n                       Address address,\n                       //...\n                       Role role\n                       ) {\n        this.id = AssertUtil.requireNotNull(id, \"Event id cannot be null\");\n        this.address = AssertUtil.requireNotNull(address, \"Address cannot be null\");\n        this.role = AssertUtil.requireNotNull(role, \"Role cannot be null\");\n        //...\n    }\n\n    // Factory method for creating new entities\n    public static UserEntity create(Address address, Role role) {\n        return new UserEntity(\n                UserId.generate(),\n                address,\n                role);\n    }\n\n    public boolean isAdmin() {\n        return role == Role.ROLE_ADMIN;\n    }\n\n    // Getters\n}\n```\n\n### Example: UserRepository\n**File:** `users/domain/repositories/UserRepository.java`\n\n```java\ninterface UserRepository extends JpaRepository<UserEntity, UserId> {\n\n    Optional<UserEntity> findByEmail(@Param(\"email\") String email);\n\n    // Convenience methods using default interface methods\n    default UserEntity getByEmail(String email) {\n        return this.findByEmail(email)\n                .orElseThrow(() -> new ResourceNotFoundException(\"User not found with email: \" + email));\n    }\n}\n```
