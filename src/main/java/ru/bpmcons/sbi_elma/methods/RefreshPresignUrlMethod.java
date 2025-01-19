package ru.bpmcons.sbi_elma.methods;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.bpmcons.sbi_elma.infra.method.Method;
import ru.bpmcons.sbi_elma.infra.version.Since;
import ru.bpmcons.sbi_elma.message.MessageWorkerService;
import ru.bpmcons.sbi_elma.models.dto.responseMq.PresignResponse;
import ru.bpmcons.sbi_elma.models.request.RefreshPresignUrlRequest;
import ru.bpmcons.sbi_elma.s3.S3FileMetadata;
import ru.bpmcons.sbi_elma.s3.S3FileUtils;
import ru.bpmcons.sbi_elma.s3.S3IdentityDocFileMetadata;
import ru.bpmcons.sbi_elma.s3.S3ModuleService;
import ru.bpmcons.sbi_elma.security.authorization.DocumentAuthorizationService;
import ru.bpmcons.sbi_elma.security.file.authorization.FileAuthorizationService;
import ru.bpmcons.sbi_elma.service.LockService;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RefreshPresignUrlMethod {
    private final S3FileUtils fileUtils;
    private final DocumentAuthorizationService documentAuthorizationService;
    private final FileAuthorizationService fileAuthorizationService;
    private final S3ModuleService service;
    private final MessageWorkerService messageWorkerService;
    private final LockService lockService;

    @Validated
    @Since(major = 1, minor = 1, patch = 19)
    @Method("${methods.refreshpresignurl?:RefreshPresignUrl}")
    public PresignResponse doMethod(RefreshPresignUrlRequest request) {
        var links = request.getLinks()
                .stream()
                .peek(item -> {
                    var meta = fileUtils.parse(item.getUrl());
                    if (meta instanceof S3IdentityDocFileMetadata) {
                        // ignore
                    } else {
                        documentAuthorizationService.verifyCurrentDoc(meta.getDocType(), meta.getContractType(), meta.getOperation());
                        FileAuthorizationService.Context ctx = fileAuthorizationService.buildContext(meta.getDocType(), meta.getContractType());
                        ctx.requirePermission(meta.getFileType(), meta.getOperation());
                    }
                })
                .collect(Collectors.toList());

        messageWorkerService.runInWorker(() -> {
            links.stream()
                    .map(item -> fileUtils.parse(item.getUrl()))
                    .parallel()
                    .collect(Collectors.groupingBy(S3FileMetadata::getDocId))
                    .forEach(lockService::restoreLock);
        });

        var items = messageWorkerService.runInWorker(() -> links.stream()
                .parallel()
                .map(item -> new PresignResponse.Item(service.refreshPresign(item.getUrl(), ru.bpmcons.sbi_elma.s3.dto.Method.PUT), item.getProperties()))
                .collect(Collectors.toList()));
        PresignResponse resp = new PresignResponse();
        resp.setLinks(items);
        resp.setResponse_code("200");
        resp.setResponse_message("Успех");
        resp.setRquid(request.getRequestId());
        return resp;
    }
}
