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

@Tags({"Voyage", "JSON", "Processeur"}) // Balises pour identifier le processeur
@CapabilityDescription("Un processeur qui traite les données de voyage au format JSON.") // Description du processeur
@SupportsBatching // Indique que le processeur prend en charge le traitement en lots
@InputRequirement(InputRequirement.Requirement.INPUT_REQUIRED) // Le processeur nécessite un FlowFile en entrée
public class TravelProcessor extends AbstractProcessor {

    // Relation "SUCCESS" pour les FlowFiles traités avec succès
    public static final Relationship SUCCESS = new Relationship.Builder()
            .name("SUCCESS")
            .description("FlowFile traité avec succès")
            .build();

    // Relation "FAILURE" pour les FlowFiles en cas d'échec du traitement
    public static final Relationship FAILURE = new Relationship.Builder()
            .name("FAILURE")
            .description("Échec du traitement du FlowFile")
            .build();

    @Override
    public Set<Relationship> getRelationships() {
        // Définir et retourner les relations possibles pour ce processeur
        final Set<Relationship> relationships = new HashSet<>();
        relationships.add(SUCCESS);
        relationships.add(FAILURE); // Ajouter la relation FAILURE
        return Collections.unmodifiableSet(relationships);
    }

    @OnScheduled
    public void onScheduled(final ProcessContext context) {
        // Cette méthode est appelée lorsque le processeur est activé ou planifié
        // Ici, aucune configuration supplémentaire n'est nécessaire
    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) {
        // Cette méthode est déclenchée chaque fois que le processeur doit traiter un FlowFile
        FlowFile flowFile = session.get(); // Obtenir le FlowFile à traiter
        if (flowFile == null) {
            return; // Si aucun FlowFile n'est disponible, arrêter le traitement
        }

        try {
            // Écrire dans le FlowFile en utilisant un StreamCallback pour lire et écrire les données
            flowFile = session.write(flowFile, new StreamCallback() {
                @Override
                public void process(InputStream in, OutputStream out) throws IOException {
                    // Lire le contenu JSON depuis le FlowFile d'entrée
                    String inputJson = readStream(in);

                    // Traiter le JSON en utilisant la méthode `processJson` de la classe `Travel`
                    String outputJson = Travel.processJson(inputJson);

                    // Écrire le JSON traité dans le FlowFile de sortie
                    out.write(outputJson.getBytes());
                }
            });

            // Transférer le FlowFile traité vers la relation SUCCESS
            session.transfer(flowFile, SUCCESS);

        } catch (Exception e) {
            // En cas d'erreur, enregistrer le message d'erreur
            getLogger().error("Échec du traitement du FlowFile", e);

            // Transférer le FlowFile vers la relation FAILURE
            session.transfer(flowFile, FAILURE);
        }
    }

    // Méthode utilitaire pour lire un InputStream et le convertir en String UTF-8
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
