package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Value("${app.allowed-root:./sandbox}")
    private String allowedRoot;

    public record DeleteRequest(List<String> paths) {}
    public record DeleteResult(List<String> deleted, List<String> refused) {}

    @PostMapping("/delete")
    public ResponseEntity<?> delete(@RequestBody DeleteRequest req) {
        if (req == null || req.paths() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "paths required"));
        }
        Path root;
        try {
            root = Paths.get(allowedRoot).toRealPath();
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("error", "allowed root missing: " + allowedRoot));
        }
        List<String> deleted = new ArrayList<>();
        List<String> refused = new ArrayList<>();
        for (String raw : req.paths()) {
            if (raw == null || raw.isBlank()) { refused.add(String.valueOf(raw)); continue; }
            try {
                Path candidate = root.resolve(raw).normalize();
                if (!candidate.startsWith(root)) { refused.add(raw); continue; }
                if (Files.isSymbolicLink(candidate)) { refused.add(raw); continue; }
                Path real = candidate.toRealPath();
                if (!real.startsWith(root)) { refused.add(raw); continue; }
                if (!Files.isRegularFile(real, LinkOption.NOFOLLOW_LINKS)) { refused.add(raw); continue; }
                Files.delete(real);
                deleted.add(raw);
            } catch (IOException | InvalidPathException e) {
                refused.add(raw);
            }
        }
        return ResponseEntity.ok(new DeleteResult(deleted, refused));
    }
}
