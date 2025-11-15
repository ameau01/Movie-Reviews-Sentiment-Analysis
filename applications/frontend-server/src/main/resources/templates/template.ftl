<#macro noauthentication title="Welcome">
    <!DOCTYPE html>
    <html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name=viewport content="width=device-width, initial-scale=1">
        <link rel="stylesheet" href="/static/styles/reset.css">
        <link rel="stylesheet" href="/static/styles/style.css">
        <link rel="icon" type="image/svg" href="/static/images/favicon.svg">
        <title>${title}</title>
    </head>
    <body>
    <header>
        <div class="container">
            <h1>AI-Powered Movie Sentiment Rating System</h1>
        </div>
        </div>
    </header>
    <section class="callout">
        <div class="container">
            A <span class="branded">CSCA-5028</span> project by <strong>alme9155</strong>
        </div>
    </section>
    <main>
        <#nested>
    </main>
    <footer>
        <div class="container">
            <script>document.write("©" + new Date().getFullYear());</script>
            Alex M (alme9155). All rights reserved.
        </div>
    </footer>
    </body>
    </html>
</#macro>
