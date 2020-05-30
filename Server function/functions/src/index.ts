import * as functions from 'firebase-functions';

import * as admin from 'firebase-admin';

var server = require('./server');
var router = require('./router');

const checksum_lib=require('./paytm/checksum')

server.start(router.route);

admin.initializeApp()

export const getCheckSum=functions.https.onRequest((request,response)=>{

    const OrderId=request.query.oId
    const CustId=request.query.custId
    const Amount=request.query.amount
    const ChannelId='WAP'
    const mId='ZBJrph96773099546467'
    const Website='WEBSTAGING'
    const CallBackUrl="https://pguat.paytm.com/paytmchecksum/paytmCallback.jsp"
    const IndustryTypeId='Retail'


 const paytmParams={
     MID:mId,
     ORDER_ID:OrderId,
     CUST_ID:CustId,
     CHANNEL_ID:ChannelId,
     TXN_AMOUNT:Amount,
     WEBSITE:Website,
     CALLBACK_URL:CallBackUrl,
     INDUSTRY_TYPE_ID:IndustryTypeId
 };

 checksum_lib.genchecksum(paytmParams,"L#eNvjWAZqJ&04qq",function(err : string , checksum : string){
     console.log(checksum);
     response.json({
         checksum
     })
 })



})



