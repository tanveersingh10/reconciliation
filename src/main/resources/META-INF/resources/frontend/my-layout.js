import { LitElement, html, css } from 'lit';
import { customElement } from 'lit/decorators.js';

@customElement('my-layout')
class MyLayout extends LitElement {
  static styles = css`
    .container {
      display: flex;
      flex-direction: column;
      align-items: center;
    }
    header, footer {
      background-color: #f1f1f1;
      padding: 1em;
      width: 100%;
      text-align: center;
    }
    main {
      flex: 1;
      width: 100%;
    }
  `;

  render() {
    return html`
      <head>
        <meta charset="UTF-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <style>
          body, #outlet {
            height: 100vh;
            width: 100%;
            margin: 0;
          }
        </style>
        <link rel="stylesheet" href="./styles/styles.css">
        <script src="./bankGrid.js"/>
        <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css">
        <!-- index.ts is included here automatically (either by the dev server or during the build) -->
      </head>
      <body>
        <h1> Body Title </h1>
        <!-- This outlet div is where the views are rendered -->
        <div id="outlet"></div>
      </body>
    `;
  }
}
