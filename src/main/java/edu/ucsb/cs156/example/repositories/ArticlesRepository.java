package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.Articles;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

/** The ArticlesRepository is a repository for Articles entities. */
@Repository
@RepositoryRestResource(exported = false)
public interface ArticlesRepository extends CrudRepository<Articles, Long> {
  /**
   * This method returns all Articles entities with a given title.
   *
   * @param title the title of the articles to find
   * @return all Articles entities with the given title
   */
  Iterable<Articles> findAllByTitle(String title);
}
