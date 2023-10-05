package com.ReadEase.ReadEase.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "document",
        indexes = {
                @Index(name = "idx_name", columnList = "name")
        }

)
@Builder()
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Document {
    @Id
    @GeneratedValue
    private int ID;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String url;
    @Column(nullable = false)
    private String thumbnailLink;
    @Column(nullable = false)
    private int numberOfPagesReading;
    @Column(nullable = false)
    private float star;
    @Column(nullable = false)
    private float size;
    @Column(nullable = false)
    private int totalPage;
    @Column(nullable = false)
    private Date lastRead;
    @Column(nullable = false)
    private Date createAt;
    @OneToMany
    @JoinColumn(name = "DOCUMENT_ID", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<Note> notes;

    @OneToMany
    @JoinColumn(name = "DOCUMENT_ID", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<HighLight> highLights;


    @ManyToMany(fetch = FetchType.LAZY,
            cascade = {
                    CascadeType.PERSIST,
                    CascadeType.MERGE
            },
            mappedBy = "documents")
    @JsonIgnore
    private Set<Collection> collections;


}
