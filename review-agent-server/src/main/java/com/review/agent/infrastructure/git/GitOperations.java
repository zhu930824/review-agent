package com.review.agent.infrastructure.git;

import com.review.agent.domain.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class GitOperations {

    @Value("${review-agent.repo-base-path:./repos}")
    private String repoBasePath;

    public void cloneRepository(String remoteUrl, String localPath, String branch) {
        File targetDir = new File(localPath);
        if (targetDir.exists()) {
            deleteDirectory(targetDir);
        }

        try (Git git = Git.cloneRepository()
                .setURI(remoteUrl)
                .setDirectory(targetDir)
                .setBranch(branch)
                .setCloneAllBranches(false)
                .call()) {
            log.info("仓库克隆成功: url={}, branch={}", remoteUrl, branch);
        } catch (GitAPIException e) {
            throw new BusinessException("GIT_CLONE_FAILED", "仓库克隆失败: " + e.getMessage(), e);
        }
    }

    public void pullRepository(String localPath, String branch) {
        try (Git git = openGit(localPath)) {
            git.pull()
                    .setRemoteBranchName(branch)
                    .call();
            log.info("仓库拉取成功: path={}, branch={}", localPath, branch);
        } catch (GitAPIException e) {
            throw new BusinessException("GIT_PULL_FAILED", "仓库拉取失败: " + e.getMessage(), e);
        }
    }

    public void fetchRepository(String localPath) {
        try (Git git = openGit(localPath)) {
            git.fetch().call();
            log.info("仓库 fetch 成功: path={}", localPath);
        } catch (GitAPIException e) {
            throw new BusinessException("GIT_FETCH_FAILED", "仓库 fetch 失败: " + e.getMessage(), e);
        }
    }

    public String getDiff(String localPath, String sourceBranch, String targetBranch) {
        try (Git git = openGit(localPath);
             RevWalk revWalk = new RevWalk(git.getRepository())) {
            Repository repo = git.getRepository();
            CanonicalTreeParser oldTree = resolveTree(repo, revWalk, targetBranch);
            CanonicalTreeParser newTree = resolveTree(repo, revWalk, sourceBranch);
            return formatDiff(git, oldTree, newTree);
        } catch (IOException e) {
            throw new BusinessException("GIT_DIFF_FAILED", "获取分支 diff 失败: " + e.getMessage(), e);
        }
    }

    public String getDiff(String localPath, String sourceCommit, String targetCommit) {
        try (Git git = openGit(localPath);
             RevWalk revWalk = new RevWalk(git.getRepository())) {
            Repository repo = git.getRepository();
            CanonicalTreeParser oldTree = resolveTreeByCommit(repo, revWalk, targetCommit);
            CanonicalTreeParser newTree = resolveTreeByCommit(repo, revWalk, sourceCommit);
            return formatDiff(git, oldTree, newTree);
        } catch (IOException e) {
            throw new BusinessException("GIT_DIFF_FAILED", "获取提交 diff 失败: " + e.getMessage(), e);
        }
    }

    public List<String> getBranches(String localPath) {
        try (Git git = openGit(localPath)) {
            List<Ref> refs = git.branchList()
                    .setListMode(ListBranchCommand.ListMode.ALL)
                    .call();
            return refs.stream()
                    .map(ref -> ref.getName().replaceAll("^refs/heads/", "")
                            .replaceAll("^refs/remotes/[^/]+/", ""))
                    .distinct()
                    .collect(Collectors.toList());
        } catch (GitAPIException e) {
            throw new BusinessException("GIT_BRANCH_FAILED", "获取分支列表失败: " + e.getMessage(), e);
        }
    }

    public String getLatestCommit(String localPath, String branch) {
        try (Git git = openGit(localPath)) {
            ObjectId objectId = git.getRepository().resolve(branch);
            if (objectId == null) {
                throw new BusinessException("GIT_RESOLVE_FAILED", "无法解析分支: " + branch);
            }
            return objectId.getName();
        } catch (IOException e) {
            throw new BusinessException("GIT_RESOLVE_FAILED", "解析最新提交失败: " + e.getMessage(), e);
        }
    }

    /**
     * 打开本地仓库，失败时抛出 BusinessException
     */
    private Git openGit(String localPath) {
        try {
            Repository repo = new FileRepositoryBuilder()
                    .setGitDir(new File(localPath, ".git"))
                    .readEnvironment()
                    .findGitDir()
                    .build();
            return new Git(repo);
        } catch (IOException e) {
            throw new BusinessException("GIT_OPEN_FAILED", "无法打开仓库: " + localPath, e);
        }
    }

    /**
     * 通过分支名解析其指向的 tree
     */
    private CanonicalTreeParser resolveTree(Repository repo, RevWalk revWalk, String branch) throws IOException {
        ObjectId objectId = repo.resolve(branch);
        if (objectId == null) {
            throw new BusinessException("GIT_RESOLVE_FAILED", "无法解析分支: " + branch);
        }
        return buildTreeParser(repo, revWalk, objectId);
    }

    /**
     * 通过 commit SHA 解析其指向的 tree
     */
    private CanonicalTreeParser resolveTreeByCommit(Repository repo, RevWalk revWalk, String commitSha) throws IOException {
        ObjectId objectId = repo.resolve(commitSha);
        if (objectId == null) {
            throw new BusinessException("GIT_RESOLVE_FAILED", "无法解析提交: " + commitSha);
        }
        return buildTreeParser(repo, revWalk, objectId);
    }

    private CanonicalTreeParser buildTreeParser(Repository repo, RevWalk revWalk, ObjectId objectId) throws IOException {
        RevCommit commit = revWalk.parseCommit(objectId);
        CanonicalTreeParser treeParser = new CanonicalTreeParser();
        try (var reader = repo.newObjectReader()) {
            treeParser.reset(reader, commit.getTree());
        }
        return treeParser;
    }

    /**
     * 将 DiffEntry 列表格式化为 unified diff 文本
     */
    private String formatDiff(Git git, CanonicalTreeParser oldTree, CanonicalTreeParser newTree)
            throws IOException, GitAPIException {
        List<DiffEntry> diffEntries = git.diff()
                .setOldTree(oldTree)
                .setNewTree(newTree)
                .call();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (DiffFormatter formatter = new DiffFormatter(out)) {
            formatter.setRepository(git.getRepository());
            formatter.format(diffEntries);
        }
        return out.toString();
    }

    /** 递归删除目录，克隆前清理已有目录 */
    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }
}
