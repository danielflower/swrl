# Swirl work

Better than JIRA, better than Trello, we've got a list!

Throw anything down in here big or small.

- In inbox view, I'd like a quick way to dismiss recommendations (Example: Dan said I should watch breaking bad. I know I'm not going to.). May need a "quick response" txt field too
- Links to buy music/books
- Swirl Creation: Should be less "open text field".  Each swirl should have a "media" (normally an image), Some default text (blurb/ track listing) and a 
"note from me", e.g. why I want you to check this awesome thing out.
- Make website not look pants.
- When a user registers as result of suggestion, immediately add suggester and suggestee to each other's network.
- Fix dates for [open graph](http://ogp.me/) and do profile linking etc
- Change type="youtube" to type="video"
- spotify integration
- Generate plain text versions of emails so links are rendered correctly
- Merge login and register pages, and honour return URLs during registration

## Bugs

- When editing a swirl, the already-added users are re-added and then added to the suggestion table again
  * add unique constraint on swirl_id / recipient_id
  * make it impossible to unselect people (they've already been emailed) or let them remove people (?)
- Paging doesn't work in the paging component because it hard-codes the paging URLs

## Tech debt

- Change the session storage to store only the user ID (or better yet, a session token) and load the user from that
on each request. Right now the whole user object is stored in a cookie, and if the user table changes the cookie data
is out of date.

## Security

- Parse out evil tags like `script` from rich taxt editors. Already uses the `enlive` lib to handle HTML parsing, so we should reuse that.
- HTTPS
- Is the long-lived cookie for remember-me secure?
- Some HTML is generated directly in clojure outside of the templating lang, and so is not properly encoded. 