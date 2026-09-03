package librarymanagement.repository;

import java.util.*;

import librarymanagement.model.Member;

public class MemberRepository {
    private Map<Integer, Member> memberMap = new HashMap<>();

    public boolean registerMember(Member member) {
        if (!memberMap.containsKey(member.getId())) {
            memberMap.put(member.getId(), member);
            System.out.println("Member registered successfully.");
            return true;
        }

        System.out.println("Member is already registered.");
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
}