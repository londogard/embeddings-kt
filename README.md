[![](https://jitpack.io/v/com.londogard/embeddings-kt.svg)](https://jitpack.io/#com.londogard/embeddings-kt)<a href='https://ko-fi.com/O5O819SEH' target='_blank'><img height='22' style='border:0px;height:22px;' src='https://az743702.vo.msecnd.net/cdn/kofi2.png?v=2' border='0' alt='Buy Me a Coffee at ko-fi.com' /></a>

:warning: This project is archived in favour of https://github.com/londogard/londogard-nlp-toolkit :warning:

londogard-nlp-toolkit contains all "general" NLP utilties for Kotlin in a small package keeping dependencies low. Tools such as the summarizer will still be kept separate.  
Word Embedding functionality exists under `com.londogard.nlp.embeddings`! :)

This library should continue to be functional though as the core logic is completed if you so wish.

----

# embeddings-kt
A easy-to-use library for Word Embeddings for JVM (written in Kotlin)

## Installation
<details open>
<summary><b>Jitpack</b> (the easiest)</summary>
<br>
Add the following to your <code>build.gradle</code>. <code>$version</code> should be equal to the version supplied by tag above.
<br>
<br>
<pre>
repositories {
  maven { url "https://jitpack.io" }
}
dependencies {
  implementation 'com.londogard:smile-nlp-kt:$version'
}        
</pre>
</details>
<details>
   <summary><b>GitHub Packages</b></summary>
<br>
Add the following to your <code>build.gradle</code>. <code>$version</code> should be equal to the version supplied by tag above.  
The part with logging into github repository is how I understand that you need to login. If you know a better way please ping me in an issue.
<br>
<br>
<pre>
repositories {
   maven {
     url = uri("https://maven.pkg.github.com/londogard/smile-nlp-kt")
     credentials {
         username = project.findProperty("gpr.user") ?: System.getenv("GH_USERNAME")
         password = project.findProperty("gpr.key") ?: System.getenv("GH_TOKEN")
     }
}
}
dependencies {
   implementation "com.londogard:smile-nlp-kt:$version"
}   
</pre>
</details>

## Usage
TODO()

## TODO
- [ ] Add usage section
- [ ] Add SIF Embeddings
- [ ] Add test for TfIdf Embeddings
- [ ] Add some kind of documentation
