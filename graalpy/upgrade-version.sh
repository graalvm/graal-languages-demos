#!/usr/bin/env bash
set -euo pipefail

# Updates version references in all GraalPy demos and optionally adds a local Maven repository.
current_version=25.0.2
new_version="${1:-}"
local_repo="${2:-}"

if [[ -z "$new_version" ]]; then
    echo "Missing version."
    echo "Usage: $0 NEW_VERSION [LOCAL_MAVEN_REPO]"
    exit 2
fi

if [[ -n "$local_repo" && "$local_repo" != file://* && "$local_repo" != http://* && "$local_repo" != https://* ]]; then
    local_repo="file://$(realpath "$local_repo")"
fi

function patch_version() {
    # ignored patterns: "sdk install java ...version..."
    local current_version_pattern="${current_version//./\\.}"
    sed -i "/sdk install java.*$current_version_pattern/!s/$current_version_pattern/$new_version/g" "$@"
}

function add_maven_repo() {
    [[ -z "$local_repo" ]] && return

    # look for pre-existing <repositories> - we must insert into it, otherwise look for "</modelVersion>"
    local indent
    if grep -q "<repositories>" pom.xml; then
        indent="$(sed -n 's/^\([[:space:]]*\)<repositories>.*/\1/p' pom.xml | head -n 1)"
        sed -i "/<repositories>/a\\
$indent  <repository>\\
$indent    <id>local-graalpy-repo</id>\\
$indent    <url>$local_repo</url>\\
$indent  </repository>" pom.xml

        sed -i "/<\\/repositories>/a\\
\\
$indent<pluginRepositories>\\
$indent  <pluginRepository>\\
$indent    <id>local-graalpy-plugin-repo</id>\\
$indent    <url>$local_repo</url>\\
$indent  </pluginRepository>\\
$indent</pluginRepositories>" pom.xml
    else
        indent="$(sed -n 's/^\([[:space:]]*\)<modelVersion>.*/\1/p' pom.xml)"
        sed -i "/<\\/modelVersion>/a\\
$indent<repositories>\\
$indent  <repository>\\
$indent    <id>local-graalpy-repo</id>\\
$indent    <url>$local_repo</url>\\
$indent  </repository>\\
$indent</repositories>\\
\\
$indent<pluginRepositories>\\
$indent  <pluginRepository>\\
$indent    <id>local-graalpy-plugin-repo</id>\\
$indent    <url>$local_repo</url>\\
$indent  </pluginRepository>\\
$indent</pluginRepositories>" pom.xml
    fi
}

function add_gradle_repo() {
    [[ -z "$local_repo" ]] && return

    if [[ -f settings.gradle.kts ]]; then
        sed -i "1i\\
pluginManagement {\\
    repositories {\\
        maven {\\
            url = uri(\"$local_repo\")\\
        }\\
        gradlePluginPortal()\\
        mavenCentral()\\
    }\\
}\\
" settings.gradle.kts
    fi

    if [[ -f settings.gradle ]]; then
        sed -i "1i\\
pluginManagement {\\
  repositories {\\
    maven {\\
      url = uri('$local_repo')\\
    }\\
    gradlePluginPortal()\\
    mavenCentral()\\
  }\\
}\\
" settings.gradle
    fi

    if [[ -f build.gradle.kts ]]; then
        sed -i "/repositories[[:space:]]*{/a\\
    maven {\\
        url = uri(\"$local_repo\")\\
    }" build.gradle.kts
    fi

    if [[ -f build.gradle ]]; then
        sed -i "/repositories[[:space:]]*{/a\\
  maven {\\
    url = uri('$local_repo')\\
  }" build.gradle
    fi
}

function add_jbang_repo() {
    [[ -z "$local_repo" ]] && return
    sed -i "/^\/\/DEPS org.graalvm.python:python-language:/i\/\/REPOS $local_repo" qrcode.java
}

root="$(cd "$(dirname "$0")" && pwd)"

cd "$root/graalpy-custom-venv-guide"
patch_version pom.xml README.md
add_maven_repo

cd "$root/graalpy-freeze-dependencies-guide"
patch_version pom.xml build.gradle.kts README.md
add_maven_repo
add_gradle_repo

cd "$root/graalpy-javase-guide"
patch_version pom.xml build.gradle.kts README.md
add_maven_repo
add_gradle_repo

cd "$root/graalpy-jbang-qrcode"
patch_version README.md qrcode.java
add_jbang_repo

cd "$root/graalpy-jython-guide"
patch_version pom.xml README.md
add_maven_repo

cd "$root/graalpy-micronaut-guide"
patch_version pom.xml build.gradle.kts README.md
add_maven_repo
add_gradle_repo

cd "$root/graalpy-micronaut-multithreaded"
patch_version pom.xml build.gradle.kts README.md
add_maven_repo
add_gradle_repo

cd "$root/graalpy-micronaut-pygal-charts"
patch_version pom.xml README.md
add_maven_repo

cd "$root/graalpy-native-extensions-guide"
patch_version pom.xml README.md
add_maven_repo

cd "$root/graalpy-openai-starter"
patch_version pom.xml build.gradle.kts README.md graalpy.lock
add_maven_repo
add_gradle_repo

cd "$root/graalpy-scripts-debug-guide"
patch_version pom.xml build.gradle.kts README.md
add_maven_repo
add_gradle_repo

cd "$root/graalpy-spring-boot-guide"
patch_version pom.xml build.gradle README.md
add_maven_repo
add_gradle_repo

cd "$root/graalpy-spring-boot-pygal-charts"
patch_version pom.xml README.md
add_maven_repo

cd "$root/graalpy-starter"
patch_version pom.xml build.gradle.kts README.md
add_maven_repo
add_gradle_repo
