package librarymanagement.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MemberTest {
    // Members below the limit can issue more books.
    @Test
    void memberCanIssueMoreBooks() {
        Member member = activeMember();

        member.addIssuedBook(101);

        assertTrue(member.canIssueMore());
    }

    // Members at the limit cannot issue more books.
    @Test
    void memberCannotExceedIssueLimit() {
        Member member = activeMember();
        for (int bookId = 1; bookId <= Member.MAX_ISSUED; bookId++) {
            member.addIssuedBook(bookId);
        }

        assertFalse(member.canIssueMore());
    }

    // Membership status determines whether the membership is active.
    @Test
    void membershipStatusIsChecked() {
        assertTrue(activeMember().hasActiveMembership());
        assertFalse(new Member(2, "Expired Member", "expired@example.com",
                "2025-01-01", "2025-12-31", "Expired").hasActiveMembership());
    }

    private Member activeMember() {
        return new Member(1, "Active Member", "active@example.com",
                "2026-01-01", "2027-01-01", "Active");
    }
}
