package ex02;

import java.io.*;
import java.util.List;
import java.util.ArrayList;

import ex02.entities.EntityFactory;
import ex02.entities.IEntity;
import ex02.entities.Scene;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Used to parse the scenes
 */
class Parser {

    private static final Logger LOG = LogManager.getLogger(Parser.class);

    private IEntity curEntity;
    private List<IEntity> entities = new ArrayList<>();
    private Scene scene;

//    public static class ParseException extends Exception {
//        static final long serialVersionUID = 1;
//
//        public ParseException(String msg) {
//            super(msg);
//        }
//    }
//
//    public Parser(Device device) {
//        Parser.device = device;
//    }

    /**
     *
     * @param in an implementation of Reader such as StringReader or FileReader
     * @throws IOException
     * @throws Exception
     */
    final Scene parse(final Reader in) throws IOException, Exception {
        final BufferedReader r = new BufferedReader(in);
        String line, curobj = null;
        int lineNum = 0;
        startFile();

        while ((line = r.readLine()) != null) {
            line = line.trim();
            ++lineNum;

            if (line.isEmpty() || (line.charAt(0) == '#')) {  // comment
                continue;
            } else if (line.charAt(line.length() - 1) == ':') { // new object;
                if (curobj != null)
                    commit();
                curobj = line.substring(0, line.length() - 1).trim().toLowerCase();
                if (!addObject(curobj))
                    reportError(String.format("Did not recognize object: %s (line %d)", curobj, lineNum));
            } else {
                int eqIndex = line.indexOf('=');
                if (eqIndex == -1) {
                    reportError(String.format("Syntax error line %d: %s", lineNum, line));
                    continue;
                }
                String name = line.substring(0, eqIndex).trim().toLowerCase();
                String[] args = line.substring(eqIndex + 1).trim().toLowerCase().split("\\s+");

                if (curobj == null) {
                    reportError(String.format("parameter with no object %s (line %d)", name, lineNum));
                    continue;
                }

                if (!setParameter(name, args))
                    reportError(String.format("Did not recognize parameter: %s of object %s (line %d)", name, curobj, lineNum));
            }
        }

        if (curobj != null) {
            commit();
        }

        endFile();

        return this.scene;
    }

    ///////////////////// override these methods in your implementation //////////////////

    private void startFile() {
        LOG.info("------ Parser started ---------");
    }

    private void endFile() throws Exception {
        if (scene != null) {
            scene.setEntities(entities);
        } else {
            throw new Exception("Scene object not found.");
        }

        LOG.info("------ Parser completed -------");
    }

    // start a new object definition
    // return true if recognized
    private boolean addObject(String name) throws Exception {
        curEntity = EntityFactory.createEntity(name);
        if (curEntity == null) {
            throw new Exception("Unknown entity encountered: " + name);
        }

        if (name.equals("scene")) {
            scene = (Scene) curEntity;
        }
        LOG.debug("OBJECT: " + name);
        return true;
    }

    // set a specific parameter for the current object
    // return true if recognized
    private boolean setParameter(String name, String[] args) {
        try {
            curEntity.setParameter(name, args);
            //System.out.print("PARAM: " + name);
            //for (String s : args)
            //    System.out.print(", " + s);
            //System.out.println();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // finish the parsing of the current object
    // here is the place to perform any validations over the parameters and
    // final initializations.
    private void commit() throws Exception {
        entities.add(curEntity);
        curEntity.postInit(entities);
    }

    private void reportError(String err) {
        System.out.println("ERROR: " + err);
    }

}

