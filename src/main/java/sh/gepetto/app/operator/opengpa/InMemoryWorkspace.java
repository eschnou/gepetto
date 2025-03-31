package sh.gepetto.app.operator.opengpa;

import org.opengpa.core.workspace.Document;
import org.opengpa.core.workspace.Workspace;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class InMemoryWorkspace implements Workspace {

    @Override
    public List<Document> getDocuments(String workspaceId) {
        return List.of();
    }

    @Override
    public Optional<Document> getDocument(String workspaceId, String name) {
        return Optional.empty();
    }

    @Override
    public Document addDocument(String workspaceId, String name, byte[] content, Map<String, String> metadata) {
        return null;
    }

    @Override
    public byte[] getDocumentContent(String workspaceId, String name) {
        return new byte[0];
    }

    @Override
    public Document addDocument(String workspaceId, String name, byte[] content) {
        return Workspace.super.addDocument(workspaceId, name, content);
    }
}
