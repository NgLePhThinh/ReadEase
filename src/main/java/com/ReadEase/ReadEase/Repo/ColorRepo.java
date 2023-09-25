package com.ReadEase.ReadEase.Repo;

import com.ReadEase.ReadEase.Model.Color;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ColorRepo extends JpaRepository<Color,Integer> {
    Optional<Color> findColorByColorHexCode(String colorHexCode);
}
