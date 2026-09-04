package librarymanagement.model;

import java.util.*;

public class Member {
    private int id;
    private String name;
    private String email;
    private String joinDate;
    private String membershipExpiryDate;
    private String membershipStatus;
    private Set<Integer> issuedBookIds = new HashSet<>();
    public static final int MAX_ISSUED = 10;

    public Member(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Member(int id, String name, String email, String joinDate,
            String membershipExpiryDate, String membershipStatus) {
        this(id, name);
        this.email = email;
        this.joinDate = joinDate;
        this.membershipExpiryDate = membershipExpiryDate;
        this.membershipStatus = membershipStatus;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return this.email;
    }

    public String getJoinDate() {
        return this.joinDate;
    }

    public String getMembershipExpiryDate() {
        return this.membershipExpiryDate;
    }

    public String getMembershipStatus() {
        return this.membershipStatus;
    }

    public Set<Integer> getIssuedBookIds() {
        return this.issuedBookIds;
    }

    public boolean canIssueMore() {
        return getIssuedBookIds().size() < MAX_ISSUED;
    }

    public boolean hasActiveMembership() {
        return "Active".equalsIgnoreCase(membershipStatus);
    }

    public boolean addIssuedBook(int bookId) {
        return getIssuedBookIds().add(bookId);
    }

    public boolean removeIssuedBook(int bookId) {
        return getIssuedBookIds().remove(bookId);
    }

    @java.lang.Override
    public java.lang.String toString() {
        String details = "ID: " + getId() + " | Name: " + getName();
        if (email != null) {
            details += " | Email: " + getEmail()
                    + " | Joined: " + getJoinDate()
                    + " | Expires: " + getMembershipExpiryDate()
                    + " | Status: " + getMembershipStatus();
        }
        return details + " | Total Books assigned: " + getIssuedBookIds().size();
    }
}