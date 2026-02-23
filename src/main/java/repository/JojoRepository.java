package repository;

import domain.Usuario;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.List;

public class JojoRepository {

    public void guardar(Object obj) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(obj);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Error al guardar en base de datos", e); // ← RELANZAR para que el Service sepa que falló
        }
    }

    public List<Usuario> listarTodos() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from Usuario", Usuario.class).list();
        }
    }

    public List<Usuario> buscarPorLinaje(String linaje) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from Usuario where linaje = :l", Usuario.class)
                    .setParameter("l", linaje)
                    .list();
        }
    }

    public List<Usuario> listarPoderosos() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("select u from Usuario u join u.stand s join s.habilidades h where h.daño > 80", Usuario.class)
                    .list();
        }
    }

    public void eliminar(Long id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Usuario u = session.get(Usuario.class, id);
            if (u != null) session.remove(u);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Error al eliminar usuario con ID: " + id, e); // ← RELANZAR también aquí
        }
    }
}