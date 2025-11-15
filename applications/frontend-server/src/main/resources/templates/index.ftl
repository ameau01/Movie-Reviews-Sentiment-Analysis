<#import "template.ftl" as t>

<@t.noauthentication title="Submit Review">
    <div class="container">
        <h2>Submit a Movie Review</h2>
        <form method="post" action="/analyze" method="post">
            <textarea name="text" placeholder="Enter your review..." required></textarea>
            <button type="submit">Analyze Sentiment</button>
        </form>
    </div>
</@t.noauthentication>
