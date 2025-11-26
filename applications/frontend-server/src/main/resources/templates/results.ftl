<#import "template.ftl" as t>

<@t.noauthentication title="Sentiment Analysis Result">
<div class="container result-page">
    <h2>Analysis Result</h2>

    <div class="review-box">
        <h3>Movie Title:</h3>
        <p class="review-text">${title}</p>
        
        <h3>Your Review Text:</h3>
        <p class="review-text">"${text}"</p>
    </div>

    <div class="result-box">
        <h3>AI Movie Rating Based on your review:</h3>
        <div class="prediction">
            <#-- Find the label with highest probability -->
            <#assign best = probabilities?sort_by("value")?reverse[0]>
            <span class="label ${best.label?replace(' ', '-')}">
                ${best.label?upper_case}
            </span>
            <p class="confidence">Confidence: ${(best.value * 100)?string("0.##")}%</p>
        </div>
    </div>

    <div class="probabilities">
        <h3>All Confidence Scores:</h3>
        <div class="bars">
            <#list probabilities as prob>
                <div class="bar-container">
                    <span class="label-text">${prob.label}</span>
                    <div class="bar-wrapper">
                        <div class="bar ${prob.label?replace(' ', '-')}" 
                             style="width: ${(prob.value * 100)?string("0.##")}%">
                            <span class="percentage">${(prob.value * 100)?string("0.##")}%</span>
                        </div>
                    </div>
                </div>
            </#list>
        </div>
    </div>

    <div class="back">
        <a href="/" class="btn">Submit Another Movie Review</a>
    </div>
</div>
</@t.noauthentication>