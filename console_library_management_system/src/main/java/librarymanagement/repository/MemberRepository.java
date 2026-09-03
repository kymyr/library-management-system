package librarymanagement.repository;

import java.util.*;

import librarymanagement.model.Member;

public class MemberRepository {
    private Map<Integer, Member> memberMap = new HashMap<>();

    public boolean registerMember(Member member) {
        if (registerMemberQuietly(member)) {
            System.out.println("Member registered successfully.");
            return true;
        }

        System.out.println("Member is already registered.");
        return false;
    }

    public boolean registerMemberQuietly(Member member) {
        if (!memberMap.containsKey(member.getId())) {
            memberMap.put(member.getId(), member);
            return true;
        }
        return false;
    }

    public Member findById(int id) {
        if (memberMap.containsKey(id)) {
            return memberMap.get(id);
        }

        System.out.println("Member is not registered.");
        return null;
    }

    public List<Member> getAllMembers() {
        return new ArrayList<Member>(memberMap.values());
    }

    public boolean containsId(int memberId) {
        return memberMap.containsKey(memberId);
    }

    public int getNextMemberId() {
        return memberMap.keySet().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0) + 1;
    }

    public void clear() {
        memberMap.clear();
    }
}