package com.trendflow.member.member.service;

import com.trendflow.member.global.code.CommonCode;
import com.trendflow.member.global.exception.NotFoundException;
import com.trendflow.member.member.entity.Member;
import com.trendflow.member.member.entity.Role;
import com.trendflow.member.member.repository.MemberRepository;
import com.trendflow.member.member.repository.RoleRepository;
import com.trendflow.member.msa.vo.LocalCode;
import com.trendflow.member.msa.service.CommonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final RoleRepository roleRepository;
    private final MemberRepository memberRepository;
    private final CommonService commonService;

    @Transactional(readOnly = true)
    public Member findMemberByMemberId(Long memberId) throws NotFoundException {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException());
        return Member.builder()
                .memberId(member.getMemberId())
                .keywordId(member.getKeywordId())
                .platformCode(member.getPlatformCode())
                .name(member.getName())
                .email(member.getEmail())
                .gender(member.getGender())
                .age(member.getAge())
                .birthday(member.getBirthday())
                .password(member.getPassword())
                .regDt(member.getRegDt())
                .roles(member.getRoles().stream().collect(Collectors.toList()))
                .build();
    }

    @Transactional(readOnly = true)
    public Member findMemberByEmail(String email) throws NotFoundException {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException());
        return Member.builder()
                .memberId(member.getMemberId())
                .keywordId(member.getKeywordId())
                .platformCode(member.getPlatformCode())
                .name(member.getName())
                .email(member.getEmail())
                .gender(member.getGender())
                .age(member.getAge())
                .birthday(member.getBirthday())
                .password(member.getPassword())
                .regDt(member.getRegDt())
                .roles(member.getRoles().stream().collect(Collectors.toList()))
                .build();
    }

    @Transactional
    public Member registMember(Member member) throws RuntimeException {
        LocalCode normalRole = commonService.getLocalCode(CommonCode.NORMAL_USER.getName());

        memberRepository.save(member);
        roleRepository.save(Role.builder()
                .roleCode(normalRole.getCode())
                .member(member)
                .build());
        return memberRepository.save(member);
    }
}
