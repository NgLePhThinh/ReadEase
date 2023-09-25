package com.ReadEase.ReadEase.Repo;

import com.ReadEase.ReadEase.Model.Color;
import com.ReadEase.ReadEase.Model.HighLight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HighLightRepo extends JpaRepository<HighLight, Integer> {
//   Optional<Color> findColorByHexCode(String colorHexCode);
}
