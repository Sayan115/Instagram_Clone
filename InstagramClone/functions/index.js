/* eslint-disable max-len */
/* eslint-disable require-jsdoc */
"use strict";

const functions = require("firebase-functions");
const nodemailer = require("nodemailer");
const gmailEmail = functions.config.gmail.email;
const gmailPassword = functions.config.gmail.password;
const mailTransport = nodemailer.createTransport({
  service: "gmail",
  auth: {
    user: gmailEmail,
    password: gmailPassword,
  },
});

const APP_NAME = "Instagram";

exports.sendWelcomeEmail = functions.auth.user().onCreate((user)=>{
  const email = user.email;
  const displayName = user.displayName;

  return sendWelcomeEmail(email, displayName);
});

async function sendWelcomeEmail(email, displayName) {
  const mailOptions={
    from: `${APP_NAME} <noreply@firebase.com`,
    to: email,
  };
  mailOptions.subject=`Welcome to ${APP_NAME}!`;
  mailOptions.text=`Hey ${displayName||" "}! Welcome to ${APP_NAME}, we hope you enjoy Instagram.`;

  await mailTransport.sendMail(mailOptions);
  console.log("New Email sent to: ", email);

  return null;
}
