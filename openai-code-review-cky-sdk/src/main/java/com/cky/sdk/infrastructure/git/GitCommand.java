package com.cky.sdk.infrastructure.git;

import com.cky.sdk.utils.RandomStringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

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
        Git git = Git.cloneRepository()
                .setURI("https://github.com/ckylucky/revirew_log") // 请确认仓库 URL 是否正确
                .setDirectory(new File("repo"))
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubToken, ""))
                .call();

        // 获取日期文件夹名称
        String dateFolderName = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        // 构建完整的文件夹路径，注意避免重复添加 ckylucky 目录
        File dateFolder = new File( dateFolderName);

        // 如果文件夹不存在，则创建
        if (!dateFolder.exists()) {
            dateFolder.mkdirs();
        }

        // 生成文件名
        String fileName = project + "-" + branch + "-" + author + "-" + System.currentTimeMillis() + "-" + RandomStringUtils.randomNumeric(4) + ".md";
        System.out.println("Generated file name: " + fileName);
        System.out.println("Full path folder: " + dateFolder.getAbsolutePath());

        // 创建文件对象并写入内容
        File newFile = new File(dateFolder, fileName);
        try (FileWriter writer = new FileWriter(newFile)) {
            writer.write(recommend);
        }

        // 提交文件到 Git
        git.add().addFilepattern(newFile.getPath().replace("\\", "/")).call(); // 注意路径分隔符，Windows 上可能需要替换
        git.commit().setMessage("Add code review new file: " + fileName).call();
        git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubToken, "")).call();

        logger.info("openai-code-review git commit and push done! {}", fileName);

        // 假设 githubReviewLogUri 是已定义的，指向 GitHub 仓库的 URL
        return "https://github.com/ckylucky/revirew_log.git" + "/blob/main/" + dateFolderName + "/ckylucky/" + fileName;
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
