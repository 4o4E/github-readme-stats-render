package top.e404.status.render.fetcher

import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import top.e404.status.render.IConfig
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class GithubFetcher(val config: IConfig) {
    private companion object {
        const val URL = "https://api.github.com/graphql"
        const val MAX_REPOS_ONE_QUERY = 100
    }

    private val client inline get() = config.client

    /**
     * 获取用户在指定时间段内的提交数量
     */
    suspend fun fetchCommitCount(username: String, end: LocalDateTime): GhResp<GhUserResp> {
        val query = $$"""
            query userContributions($username: String!, $from: DateTime!, $to: DateTime!) {
                user(login: $username) {
                    contributionsCollection(from: $from, to: $to) {
                        contributionCalendar {
                            weeks {
                                contributionDays {
                                    contributionCount
                                    date
                                }
                            }
                        }
                    }
                }
            }
        """.replace(Regex("\\s{2,}"), " ")
        return client.post(URL) {
            header("Authorization", "bearer ${config.githubToken}")
            setBody(Json.encodeToString(buildJsonObject {
                put("query", JsonPrimitive(query))
                put("variables", buildJsonObject {
                    put("username", username)
                    put("from", end.minusYears(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    put("to", end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                })
            }))
        }.bodyAsText().let { Json.decodeFromString<GhResp<GhUserResp>>(it) }
    }

    /**
     * 获取用户提交详情
     */
    suspend fun fetchDetail(userName: String, maxRepos: Int): GhUser? {
        val res1 = fetchDetailFirst(userName)
        val user = res1.data.user ?: return null
        val repos = user.repositories!!
        val nodes = repos.nodes.toMutableList()
        if (nodes.size == MAX_REPOS_ONE_QUERY) {
            val edges = repos.edges
            var cursor = edges.last().cursor
            while (nodes.size < maxRepos) {
                val res2 = fetchDetailNext(userName, cursor)
                val repos2 = res2.data.user?.repositories ?: break
                nodes.addAll(repos2.nodes)
                if (repos2.nodes.size != MAX_REPOS_ONE_QUERY) {
                    break
                }
                cursor = repos2.edges.last().cursor
            }
        }
        return user

    }

    private suspend fun fetchDetailFirst(username: String): GhResp<GhUserResp> {
        val query = $$"""
            query($login: String!) {
                user(login: $login) {
                    contributionsCollection {
                        contributionCalendar {
                            isHalloween
                            totalContributions
                            weeks {
                                contributionDays {
                                    contributionCount
                                    contributionLevel
                                    date
                                }
                            }
                        }
                        commitContributionsByRepository(maxRepositories: $${MAX_REPOS_ONE_QUERY}) {
                            repository {
                                primaryLanguage {
                                    name
                                    color
                                }
                            }
                            contributions {
                                totalCount
                            }
                        }
                        totalCommitContributions
                        totalIssueContributions
                        totalPullRequestContributions
                        totalPullRequestReviewContributions
                        totalRepositoryContributions
                    }
                    repositories(first: $${MAX_REPOS_ONE_QUERY}, ownerAffiliations: OWNER) {
                        edges {
                            cursor
                        }
                        nodes {
                            forkCount
                            stargazerCount
                        }
                    }
                }
            }
        """.replace(Regex("\\s{2,}"), " ")
        return client.post(URL) {
            header("Authorization", "bearer ${config.githubToken}")
            setBody(Json.encodeToString(buildJsonObject {
                put("query", JsonPrimitive(query))
                put("variables", buildJsonObject {
                    put("login", username)
                })
            }))
        }.bodyAsText().let { Json.decodeFromString<GhResp<GhUserResp>>(it) }
    }

    private suspend fun fetchDetailNext(username: String, cursor: String): GhResp<GhUserResp> {
        val query = $$"""
            query($login: String!, $cursor: String!) {
                user(login: $login) {
                    repositories(after: $cursor, first: $${MAX_REPOS_ONE_QUERY}, ownerAffiliations: OWNER) {
                        edges {
                            cursor
                        }
                        nodes {
                            forkCount
                            stargazerCount
                        }
                    }
                }
            }
        """.replace(Regex("\\s{2,}"), " ")
        return client.post(URL) {
            header("Authorization", "bearer ${config.githubToken}")
            setBody(Json.encodeToString(buildJsonObject {
                put("query", JsonPrimitive(query))
                put("variables", buildJsonObject {
                    put("login", username)
                    put("cursor", cursor)
                })
            }))
        }.bodyAsText().let { Json.decodeFromString<GhResp<GhUserResp>>(it) }
    }
}