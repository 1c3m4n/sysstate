package nl.unionsoft.sysstate.common.stateresolver.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Properties;

import nl.unionsoft.sysstate.common.dto.StateDto;
import nl.unionsoft.sysstate.common.enums.StateType;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

public class JSONStateResolverPluginImpl extends HttpStateResolverPluginImpl {

    private static final Logger LOG = LoggerFactory.getLogger(JSONStateResolverPluginImpl.class);

    @Override
    public void handleEntity(final HttpEntity httpEntity, Properties configuration, final StateDto state) throws IOException {
        InputStream contentStream = null;
        try {
            if (httpEntity != null) {
                contentStream = httpEntity.getContent();
                Gson gson = new Gson();
                JsonReader json = new JsonReader(new InputStreamReader(contentStream));
                while (json.hasNext()) {
                    // json.next
                }
                JsonElement jsonElement = new JsonElement() {
                };
                // gson.fromJson(json, classOfT)

                // handleGSON(document, configuration, state);
            }
        } catch(final IllegalStateException e) {
            LOG.error("Caught IllegalStateException", e);
            state.setState(StateType.ERROR);
            state.setDescription(e.getMessage());

        } catch(final IOException e) {
            LOG.error("Caught IOException", e);
            state.setState(StateType.ERROR);
            state.setDescription(e.getMessage());
        } finally {
            IOUtils.closeQuietly(contentStream);
        }
    }

}
