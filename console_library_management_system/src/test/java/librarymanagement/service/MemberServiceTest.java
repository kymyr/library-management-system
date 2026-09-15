package librarymanagement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import librarymanagement.model.Member;
import librarymanagement.repository.MemberRepository;
import org.junit.jupiter.api.Test;

class MemberServiceTest {
    // CsvService writes to the real data/ folder, so tests use a dummy data.
    private static class NoOpCsvService extends CsvService {
        @Override
        public int saveMembers(MemberRepository memberRepository) {
            return 0;
        }
    }

    private final MemberRepository memberRepo = new MemberRepository();
    private final MemberService memberService = new MemberService(memberRepo, new NoOpCsvService());

    // A built member gets a generated ID, a full name, and a one-year expiry.
    @Test
    void buildMemberFillsInDetails() {
        Member member = memberService.buildMember("Jane", "Doe", "jane@example.com",
                LocalDate.of(2026, 1, 1));

        assertEquals(1, member.getId());
        assertEquals("Jane Doe", member.getName());
        assertEquals("2026-01-01", member.getJoinDate());
        assertEquals("2027-01-01", member.getMembershipExpiryDate());
        assertEquals("Active", member.getMembershipStatus());
    }

    // Registering a new member adds it to the repository.
    @Test
    void registerAddsMember() throws Exception {
        Member member = memberService.buildMember("Jane", "Doe", "jane@example.com", LocalDate.now());

        assertTrue(memberService.register(member));
        assertEquals(1, memberRepo.getAllMembers().size());
    }

    // Registering the same member ID twice is rejected.
    @Test
    void registerRejectsDuplicateMember() throws Exception {
        Member member = memberService.buildMember("Jane", "Doe", "jane@example.com", LocalDate.now());
        memberService.register(member);

        assertFalse(memberService.register(member));
        assertEquals(1, memberRepo.getAllMembers().size());
    }
}
