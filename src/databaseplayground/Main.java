/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package databaseplayground;

import java.util.Iterator;
import java.util.List;
import org.biofab.hibernate.HibernateUtil;
import org.biofab.model.DNAMolecule;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 *
 * @author juul
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Session session = HibernateUtil.getSessionFactory().openSession();

        System.out.println("Running HQL query.");

        String hql = "from DNAMolecule";

        Query q = session.createQuery(hql);
        session.beginTransaction();
        List resultList = q.list();
        Iterator it = resultList.iterator();
        while(it.hasNext()) {
            DNAMolecule d = (DNAMolecule) it.next();
            System.out.println("d.getId() = " + d.getId());
        }

        session.getTransaction().commit();

        session.close();
    }

}
