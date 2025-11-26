<#import "template.ftl" as t>

<@t.noauthentication title="Submit Review">
    <div class="container">
        <h2>Submit a Movie Review</h2>
        <form method="post" action="/analyze" method="post">
            <input name="title" placeholder="Name of the movie" required></input>
            <textarea name="text" placeholder="Enter your movie review..." required></textarea>
            <button type="submit">Analyze Review Sentiment</button>
        </form>
    </div>
</@t.noauthentication>
