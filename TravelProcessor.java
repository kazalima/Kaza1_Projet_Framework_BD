package sda.datastreaming.processor;

import org.apache.nifi.annotation.behavior.SupportsBatching;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.io.StreamCallback;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import sda.datastreaming.Travel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Tags({"Travel", "JSON", "Processor"})
@CapabilityDescription("A processor that processes travel data from JSON.")
@SupportsBatching
@InputRequirement(InputRequirement.Requirement.INPUT_REQUIRED)
public class TravelProcessor extends AbstractProcessor {

    public static final Relationship SUCCESS = new Relationship.Builder()
            .name("SUCCESS")
            .description("Successfully processed FlowFile")
            .build();

    @Override
    public Set<Relationship> getRelationships() {
        final Set<Relationship> relationships = new HashSet<>();
        relationships.add(SUCCESS);
        return Collections.unmodifiableSet(relationships);
    }

    @OnScheduled
    public void onScheduled(final ProcessContext context) {
        // Any setup code
    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) {
        FlowFile flowFile = session.get();
        if (flowFile == null) {
            return;
        }

        try {
            flowFile = session.write(flowFile, new StreamCallback() {
                @Override
                public void process(InputStream in, OutputStream out) throws IOException {
                    // Lire le JSON depuis le FlowFile d'entrée
                    String inputJson = readStream(in);

                    // Traiter le JSON en utilisant la classe Travel
                    String outputJson = Travel.processJson(inputJson);

                    // Écrire le JSON traité dans le FlowFile de sortie
                    out.write(outputJson.getBytes());
                }
            });

            // Transférer le FlowFile vers la relation SUCCESS
            session.transfer(flowFile, SUCCESS);

        } catch (Exception e) {
            getLogger().error("Failed to process the FlowFile", e);
            session.remove(flowFile);
        }
    }

    private String readStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString("UTF-8");
    }
}
