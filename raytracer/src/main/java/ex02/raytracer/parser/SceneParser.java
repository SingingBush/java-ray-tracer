package ex02.raytracer.parser;

import ex02.entities.EntityFactory;
import ex02.entities.IEntity;
import ex02.entities.Scene;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Samael Bate (singingbush)
 * created on 18/08/17
 */
public class SceneParser implements Parser<Scene> {

    private static final Logger LOG = LoggerFactory.getLogger(SceneParser.class);

    private final Reader _reader;
    private final String _filePath; // dir path to location of the scene file being loaded
    private final String _fileName; // the file name will be used as the name of the scene

    private IEntity _curEntity;
    private List<IEntity> _entities = new ArrayList<>();
    private Scene _scene;

    public SceneParser(final File file) throws IOException {
        if(file != null && file.canRead()) {
            _filePath = file.getParent();
            _fileName = file.getName();
            _reader = Files.newBufferedReader(file.toPath());
        } else {
            _filePath = null;
            _fileName = null;
            _reader = null;
        }
    }

    @Deprecated
    public SceneParser(final Reader reader) {
        _reader = reader;
        _filePath = null;
        _fileName = null;
    }

    public SceneParser(final String scenePath, final Reader reader) {
        _filePath = scenePath;
        _fileName = null;
        _reader = reader;
    }

    @Deprecated
    public SceneParser(final String sceneData) {
        _reader = new StringReader(sceneData);
        _filePath = null;
        _fileName = null;
    }

    public SceneParser(final String scenePath, final String sceneData) {
        _filePath = scenePath;
        _fileName = null;
        _reader = new StringReader(sceneData);
    }

    @Override
    public Scene parse() throws ParserException {
        if(_reader == null) {
            throw new ParserException("Reader was null"); // fail fast if there's no Reader
        }

        try(final BufferedReader buffer = new BufferedReader(_reader)) {
            String line, curobj = null;
            int lineNum = 0;
            onStart();

            while ((line = buffer.readLine()) != null) {
                line = line.trim();
                ++lineNum;

                if (line.isEmpty() || (line.charAt(0) == '#')) {  // comment
                    continue;
                } else if (line.charAt(line.length() - 1) == ':') { // new object;
                    if (curobj != null)
                        commit();
                    curobj = line.substring(0, line.length() - 1).trim().toLowerCase();
                    if (!addObject(curobj))
                        LOG.error(String.format("Did not recognize object: %s (line %d)", curobj, lineNum));
                } else {
                    int eqIndex = line.indexOf('=');
                    if (eqIndex == -1) {
                        LOG.error(String.format("Syntax error line %d: %s", lineNum, line));
                        continue;
                    }
                    String name = line.substring(0, eqIndex).trim().toLowerCase();
                    String[] args = line.substring(eqIndex + 1).trim().toLowerCase().split("\\s+");

                    if (curobj == null) {
                        LOG.error(String.format("parameter with no object %s (line %d)", name, lineNum));
                        continue;
                    }

                    if (!setParameter(name, args))
                        LOG.error(String.format("Did not recognize parameter: %s of object %s (line %d)", name, curobj, lineNum));
                }
            }

            if (curobj != null) {
                commit();
            }

            onFinishing();

            return _scene;
        } catch (final IOException e) {
            LOG.error("IOException while parsing scene file", e);
            throw new ParserException("Could not read from buffer", e);
        }
    }

    private void onStart() {
        LOG.info("------ Parser started ---------");
    }

    private void onFinishing() throws ParserException {
        if (_scene != null) {
            try {
                _scene.setEntities(_entities);
            } catch (final Exception e) {
                LOG.error("There was a problem setting entities on scene", e);
                throw new ParserException(e.getMessage(), e);
            }
            _scene.setName(_fileName != null ? _fileName : "Unknown Scene");
        } else {
            throw new ParserException("Scene object not found.");
        }

        LOG.info("------ Parser completed -------");
    }

    // start a new object definition
    // return true if recognized
    private boolean addObject(final String name) throws ParserException {
        _curEntity = EntityFactory.createEntity(name);
        if (_curEntity == null) {
            throw new ParserException("Unknown entity encountered: " + name);
        }

        if (name.equals("scene")) {
            _scene = (Scene) _curEntity;
        }
        LOG.debug("entity: " + name);
        return true;
    }

    // set a specific parameter for the current object
    // return true if recognized
    private boolean setParameter(final String name, final String[] args) {
        try {
            if("texture".equalsIgnoreCase(name)) {
                _curEntity.setParameter(name, new String[]{ Paths.get(_filePath, args).toString() });
            } else {
                _curEntity.setParameter(name, args);
            }
            LOG.debug("\t{} = {}", name, args);
            return true;
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            return false;
        }
    }

    // finish the parsing of the current object
    // here is the place to perform any validations over the parameters and
    // final initializations.
    private void commit() throws ParserException {
        _entities.add(_curEntity);
        try {
            _curEntity.postInit(_entities);
        } catch (final Exception e) {
            LOG.error("There was a problem calling postInit on current entity", e);
            throw new ParserException("Scene Parser could not commit()", e);
        }
    }
}
