
package org.biofab.playground;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Connection;

public class PromoterLibraryAssembler
{
    String      _jdbcDriver = "jdbc:postgresql://localhost:5432/biofab";
    String      _user = "biofab";
    String      _password = "fiobab";
    Connection  _connection = null;
    
    public static void main(String[] args)
    {
        PromoterLibraryAssembler p = new PromoterLibraryAssembler();
    }

    public PromoterLibraryAssembler()
    {
        Statement           statement = null;
        Statement           seqStatement = null;
        ResultSet           designs = null;
        ResultSet           sequences;
        String              partSeq = null;
        String              backboneSeq = null;
        Pattern             pattern = null;
        Matcher             matcher = null;
        int                 start;
        int                 stop;
        String              designID;
        String              featureID;
        String              updateSQL;
        //ArrayList<String>   updates;
        String              backbone = null;
        String              construct;

        try
        {
            _connection = DriverManager.getConnection(_jdbcDriver, _user, _password);
            statement = _connection.createStatement();
            seqStatement = _connection.createStatement();
            designs = statement.executeQuery("select * from modular_promoter_library_view;");
            sequences = seqStatement.executeQuery("select * from dna_sequence;");
            //updates = new ArrayList<String>();

            while (sequences.next())
            {
                backbone = sequences.getString("nucleotides");
            }

            while (designs.next())
            {
                designID = designs.getString("design_id");
                partSeq = designs.getString("part_dna_sequence");
                construct = backbone.replace("*", partSeq);
                System.out.println(designID + "," + construct);
            }
        }
        catch (SQLException ex)
        {
            System.out.println(ex.getMessage());
        }
        finally
        {
            try
            {
                _connection.close();
            }
            catch (SQLException ex)
            {
                System.out.println(ex.getMessage());
            }
        }

        System.out.println("Done");
    }
}
