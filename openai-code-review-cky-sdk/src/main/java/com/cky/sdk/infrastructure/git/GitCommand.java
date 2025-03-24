package com.cky.sdk.infrastructure.git;

import com.cky.sdk.utils.RandomStringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class GitCommand {

    private final Logger logger = LoggerFactory.getLogger(GitCommand.class);

    private final String githubReviewLogUri;

    private final String githubToken;

    private final String project;

    private final String branch;

    private final String author;

    private final String message;

    public GitCommand(String githubReviewLogUri, String githubToken, String project, String branch, String author, String message) {
        this.githubReviewLogUri = githubReviewLogUri;
        this.githubToken = githubToken;
        this.project = project;
        this.branch = branch;
        this.author = author;
        this.message = message;
    }

    public String diff() throws IOException, InterruptedException {
        // openai.itedus.cn
        ProcessBuilder logProcessBuilder = new ProcessBuilder("git", "log", "-1", "--pretty=format:%H");
        logProcessBuilder.directory(new File("."));
        Process logProcess = logProcessBuilder.start();

        BufferedReader logReader = new BufferedReader(new InputStreamReader(logProcess.getInputStream()));
        String latestCommitHash = logReader.readLine();
        logReader.close();
        logProcess.waitFor();

        ProcessBuilder diffProcessBuilder = new ProcessBuilder("git", "diff", latestCommitHash + "^", latestCommitHash);
        diffProcessBuilder.directory(new File("."));
        Process diffProcess = diffProcessBuilder.start();

        StringBuilder diffCode = new StringBuilder();
        BufferedReader diffReader = new BufferedReader(new InputStreamReader(diffProcess.getInputStream()));
        String line;
        while ((line = diffReader.readLine()) != null) {
            diffCode.append(line).append("\n");
        }
        diffReader.close();

        int exitCode = diffProcess.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Failed to get diff, exit code:" + exitCode);
        }

        return diffCode.toString();
    }
    public String commitAndPush(String recommend) throws Exception {
        // 使用明确的项目根目录路径
        File repoDir = new File(System.getProperty("user.dir"), "repo");


        // 1. 克隆仓库（带完整校验）
        Git git = Git.cloneRepository()
                .setURI(githubReviewLogUri + ".git")
                .setDirectory(repoDir)
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubToken, ""))
                .call();

        // 验证仓库元数据
        if (!new File(repoDir, ".git").exists()) {
            throw new IOException("Repository metadata not found");
        }

        // 2. 生成准确的时间戳目录
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        String dateFolderName = sdf.format(new Date());

        File dateFolder = new File(repoDir, dateFolderName);
        if (!dateFolder.exists() && !dateFolder.mkdirs()) {
            throw new IOException("Directory creation failed: " + dateFolder.getAbsolutePath());
        }

        // 3. 创建文件（带路径校验）
        String fileName = String.format("%s-%s-%s-%d-%s.md",
                project, branch, author,
                System.currentTimeMillis(),
                RandomStringUtils.randomNumeric(4));

        File newFile = new File(dateFolder, fileName);
        try (FileWriter writer = new FileWriter(newFile)) {
            writer.write(recommend);
        } catch (IOException e) {
            throw new IOException("Failed to write file: " + newFile.getAbsolutePath(), e);
        }

        // 4. 提交并推送（带异常捕获）
        try {
            git.add().addFilepattern(dateFolderName + "/" + fileName).call();
            git.commit().setMessage("Add review: " + fileName).call();
            git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubToken, "")).call();
        } catch (GitAPIException e) {
            throw new RuntimeException("Git operation failed", e);
        }

        logger.info("Successfully pushed: {}", newFile.getAbsolutePath());
        return githubReviewLogUri + "/blob/master/" + dateFolderName + "/" + fileName;
    }


    public String getProject() {
        return project;
    }

    public String getBranch() {
        return branch;
    }

    public String getAuthor() {
        return author;
    }

    public String getMessage() {
        return message;
    }
}
