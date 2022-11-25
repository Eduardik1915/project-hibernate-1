package com.game.repository;

import com.game.entity.Player;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;




@Repository(value = "db")
public class PlayerRepositoryDB implements IPlayerRepository {

    private final SessionFactory sessionFactory;

    public PlayerRepositoryDB() {
        Properties properties = new Properties();
        properties.put(Environment.DRIVER, "com.mysql.cj.jdbc.Driver");
        properties.put(Environment.URL, "jdbc:mysql://localhost:3306/rpg");
        properties.put(Environment.DIALECT, "org.hibernate.dialect.MySQL8Dialect");
        properties.put(Environment.USER, "root");
        properties.put(Environment.PASS, "rootPass#");
        properties.put(Environment.HBM2DDL_AUTO, "update");
        properties.put(Environment.SHOW_SQL, "true");
        sessionFactory = new Configuration()
                .addProperties(properties)
                .addAnnotatedClass(Player.class)
                .buildSessionFactory();
    }

    @Override
    public List<Player> getAll(int pageNumber, int pageSize) {
        try (Session session = sessionFactory.openSession()) {
            String nativeQuery = "select * from player";
            Query<Player> query = session.createNativeQuery(nativeQuery, Player.class);
            query.setFirstResult(pageNumber * pageSize);
            query.setMaxResults(pageSize);
            return query.list();
        }
    }

    @Override
    public int getAllCount() {
        try (Session session = sessionFactory.openSession()) {
            Query<Long> query = session.createNamedQuery("PlayersCount", Long.class);
            return query.uniqueResult().intValue();
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public Player save(Player player) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            if (Objects.nonNull(player)) {
                session.persist(player);
                session.getTransaction().commit();
            }
        } catch (Exception e) {
            sessionFactory.getCurrentSession().getTransaction().rollback();
        }
        return player;
    }

    @Override
    public Player update(Player player) {
        try(Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            if (Objects.nonNull(player)) {
                session.merge(player);
                session.getTransaction().commit();
            }
        } catch (Exception e) {
            sessionFactory.getCurrentSession().getTransaction().rollback();
        }
        return player;
    }

    @Override
    public Optional<Player> findById(long id) {
        try(Session session = sessionFactory.openSession()) {
            Player player = session.find(Player.class, id);
            return Optional.of(player);
        }
    }

    @Override
    public void delete(Player player) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            if (Objects.nonNull(player)) {
                session.remove(player);
            }
            session.getTransaction().commit();
        } catch (Exception e) {
            sessionFactory.getCurrentSession().getTransaction().rollback();
        }
    }

    @PreDestroy
    public void beforeStop() {
        sessionFactory.close();
    }
}