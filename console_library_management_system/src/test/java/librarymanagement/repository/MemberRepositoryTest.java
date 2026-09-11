package librarymanagement.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import librarymanagement.exception.MemberNotFoundException;
import librarymanagement.model.Member;
import org.junit.jupiter.api.Test;

class MemberRepositoryTest {
    // Members can be registered and found.
    @Test
    void registersAndFinds() {
        MemberRepository memberRepository = new MemberRepository();
        Member member = new Member(101, "Test Member");

        assertTrue(memberRepository.registerMember(member));
        assertEquals(member, memberRepository.findById(101));
        assertEquals(1, memberRepository.getAllMembers().size());
    }

    // Duplicate member IDs are rejected.
    @Test
    void rejectsDuplicateIds() {
        MemberRepository memberRepository = new MemberRepository();

        assertTrue(memberRepository.registerMember(new Member(102, "First Member")));
        assertFalse(memberRepository.registerMember(new Member(102, "Duplicate Member")));
        assertEquals(1, memberRepository.getAllMembers().size());
    }

    // Missing members return null from the regular lookup.
    @Test
    void missingMemberReturnsNull() {
        MemberRepository memberRepository = new MemberRepository();

        assertNull(memberRepository.findById(999));
    }

    // The next ID is one greater than the highest existing ID.
    @Test
    void generatesNextId() {
        MemberRepository memberRepository = new MemberRepository();
        memberRepository.registerMemberQuietly(new Member(101, "First Member"));
        memberRepository.registerMemberQuietly(new Member(205, "Second Member"));

        assertEquals(206, memberRepository.getNextMemberId());
    }

    // Missing IDs raise the member exception.
    @Test
    void throwsForMissingMember() {
        MemberRepository memberRepository = new MemberRepository();

        assertThrows(MemberNotFoundException.class, () -> memberRepository.findByIdOrThrow(999));
    }
}