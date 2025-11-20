package com.nrd.annonceservice.repository;

import com.nrd.annonceservice.entity.Annonce;
import com.nrd.annonceservice.entity.Categorie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategorieRepository  extends JpaRepository<Categorie, Long> {

}
