package repository;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            // .configure() busca por defecto el archivo "hibernate.cfg.xml"
            return new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Fallo inicial en la creación de SessionFactory: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}