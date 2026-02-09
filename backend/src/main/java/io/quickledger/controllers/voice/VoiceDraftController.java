package io.quickledger.controllers.voice;

import io.quickledger.dto.voice.VoiceDraftResponse;
import io.quickledger.entities.User;
import io.quickledger.security.UserIdAuth;
import io.quickledger.services.voice.VoiceDraftService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/companies/{companyId}/voice")
public class VoiceDraftController {
    private final VoiceDraftService voiceDraftService;

    public VoiceDraftController(VoiceDraftService voiceDraftService) {
        this.voiceDraftService = voiceDraftService;
    }

    @PostMapping(value = "/draft", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VoiceDraftResponse> createDraft(
            @PathVariable Long companyId,
            @RequestParam("audio") MultipartFile audio,
            @RequestParam(value = "documentType", required = false) String documentType,
            @RequestParam(value = "languageCode", required = false) String languageCode,
            @RequestParam(value = "sampleRateHertz", required = false) Integer sampleRateHertz,
            @RequestParam(value = "encoding", required = false) String encoding,
            @UserIdAuth final User user) {
        VoiceDraftResponse response = voiceDraftService.createDraftFromAudio(
                companyId,
                audio,
                documentType,
                languageCode,
                sampleRateHertz,
                encoding,
                user);
        return ResponseEntity.ok(response);
    }
}
