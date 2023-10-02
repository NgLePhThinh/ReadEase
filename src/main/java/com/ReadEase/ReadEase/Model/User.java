package com.ReadEase.ReadEase.Model;


import com.ReadEase.ReadEase.Config.StringPrefixedSequenceIdGenerator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Parameter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.stream.Collectors;


@Entity
@Data
@Table (name="user")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @GenericGenerator(
            name = "user_seq",
            strategy = "com.ReadEase.ReadEase.Config.StringPrefixedSequenceIdGenerator",
            parameters = {
                @Parameter(name = StringPrefixedSequenceIdGenerator.INCREMENT_PARAM, value = "50"),
                @Parameter(name = StringPrefixedSequenceIdGenerator.VALUE_PREFIX_PARAMETER, value = "RE"),
                @Parameter(name = StringPrefixedSequenceIdGenerator.NUMBER_FORMAT_PARAMETER, value = "%06d") })

    @Column(length = 8)
    private String ID;
    @Column(nullable = false,unique = true)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String avatar;
    @Column(nullable = false)
    private Date lastAccess;
    @Column(nullable = false)
    private Date createAt;
    @Column(nullable = false)
    private long totalAccessTime;

    @ManyToOne
    @JoinColumn(name = "ROLE_ID",nullable = false)
    private Role role;

    @OneToMany(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JoinColumn(name ="USER_ID",nullable = false)
    @OnDelete(action =  OnDeleteAction.CASCADE)
    private Set<Document> documents;

    @OneToMany(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JoinColumn(name ="USER_ID",nullable = false)
    @OnDelete(action =  OnDeleteAction.CASCADE)
    private Set<Collection> collections;

    public User(String email, String password, Role role) {
        this.email = email;
        this.password = password;
        this.avatar = "";
        this.lastAccess = new Date();
        this.createAt = new Date();
        this.totalAccessTime = 0;
        this.role = role;
    }
    public User(String ID){
        this.ID = ID;
    }
    public User(String ID, String avatar){
        this.ID = ID;
        this.avatar = avatar;
    }



    public Set<Document> getDocumentsSortedByLastReadDesc() {
        Comparator<Document> descendingComparator =
                (doc1, doc2) -> doc2.getCreateAt().compareTo(doc1.getLastRead());

        TreeSet<Document> sortedSet = new TreeSet<>(descendingComparator);

        sortedSet.addAll(documents);
        return sortedSet;
    }

    public Set<Document> getDocumentCustom(int skip, int limit){
        return  documents.stream()
                .skip(skip)
                .limit(limit)
                .collect(Collectors.toSet());
    }

    public Document getLastReadingDocument(){

        Optional<Document> latestDocument = documents.stream()
                .filter(doc -> doc.getLastRead() != null)
                .max(Comparator.comparing(Document::getLastRead));

        return latestDocument.orElse(null);
    }

    @Override
    public java.util.Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(this.role.getRole()));
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
