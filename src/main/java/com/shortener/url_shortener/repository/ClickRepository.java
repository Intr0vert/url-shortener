package com.shortener.url_shortener.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.shortener.url_shortener.model.Click;

public interface ClickRepository extends JpaRepository<Click, Long> {

        long countByLinkId(Long linkId);

        @Query("SELECT CAST(c.createdAt AS date), COUNT(c) " +
                        "FROM Click c WHERE c.link.id = :linkId " +
                        "GROUP BY CAST(c.createdAt AS date) " +
                        "ORDER BY CAST(c.createdAt AS date) DESC")
        List<Object[]> countByDay(Long linkId);

        @Query("SELECT c.country, COUNT(c) FROM Click c " +
                        "WHERE c.link.id = :linkId AND c.country IS NOT NULL " +
                        "GROUP BY c.country ORDER BY COUNT(c) DESC")
        List<Object[]> topCountries(Long linkId);

        @Query("SELECT c.deviceType, COUNT(c) FROM Click c " +
                        "WHERE c.link.id = :linkId " +
                        "GROUP BY c.deviceType ORDER BY COUNT(c) DESC")
        List<Object[]> topDevices(Long linkId);

        @Query("SELECT c.browser, COUNT(c) FROM Click c " +
                        "WHERE c.link.id = :linkId " +
                        "GROUP BY c.browser ORDER BY COUNT(c) DESC")
        List<Object[]> topBrowsers(Long linkId);

        @Query("SELECT c.referer, COUNT(c) FROM Click c " +
                        "WHERE c.link.id = :linkId AND c.referer IS NOT NULL AND c.referer != '' " +
                        "GROUP BY c.referer ORDER BY COUNT(c) DESC")
        List<Object[]> topReferers(Long linkId);

        void deleteByLinkId(Long linkId);
}