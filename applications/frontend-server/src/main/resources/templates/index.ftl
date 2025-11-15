<#import "template.ftl" as layout>
<@t.noauthentication title="Submit Review">
    <div class="container">
        <h2>Submit a Movie Review</h2>
        <form method="post" action="/analyze">
            <textarea name="text" placeholder="Enter your review..." required></textarea>
            <button type="submit">Analyze Sentiment</button>
        </form>
    </div>
</@layout.noauthentication>
