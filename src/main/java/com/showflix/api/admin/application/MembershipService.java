package com.showflix.api.admin.application;

import com.showflix.api.admin.domain.Membership;
import com.showflix.api.admin.domain.MembershipRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MembershipService {

    private final MembershipRepository repository;

    public MembershipService(MembershipRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Membership> getAll() {
        return repository.findAll();
    }

    @Transactional
    public void create(String memberName, String phone, String joinDate,
                       String expireDate, String memo) {
        if (memberName == null || memberName.isBlank()) {
            throw new IllegalArgumentException("회원명을 입력해주세요.");
        }

        Membership entity = new Membership();
        entity.setMemberName(memberName);
        entity.setPhone(phone);
        entity.setJoinDate(joinDate);
        entity.setExpireDate(expireDate);
        entity.setMemo(memo);

        repository.insert(entity);
    }

    @Transactional
    public void update(Long id, String memberName, String phone, String joinDate,
                       String expireDate, String memo) {
        if (memberName == null || memberName.isBlank()) {
            throw new IllegalArgumentException("회원명을 입력해주세요.");
        }

        repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다: " + id));

        Membership entity = new Membership();
        entity.setId(id);
        entity.setMemberName(memberName);
        entity.setPhone(phone);
        entity.setJoinDate(joinDate);
        entity.setExpireDate(expireDate);
        entity.setMemo(memo);

        repository.update(entity);
    }

    @Transactional
    public void delete(Long id) {
        repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다: " + id));
        repository.deleteById(id);
    }
}
