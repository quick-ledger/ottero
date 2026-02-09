import React from 'react';
import ReactMarkdown from 'react-markdown';


const markdownContent = `
# Terms Of Service

## Terms of Service Agreement

Our terms and conditions, which all customers must agree to, are as follows:

Your use of Ottero products, services, and website is subject to the terms of this legal agreement. "Ottero," "we," and "our" mean Ottero Pty Ltd. "Products," "Plans," "services," and "website" are referred to collectively in this document as "services." By using Ottero services, you consent to be bound by this agreement. You may not use our services if you are not of legal age to form a binding contract or if you are barred for any reason from receiving our services under the law.

Ottero reserves the right to revise or amend any portion of this agreement at any time, at its sole discretion, and without advance notice to you. Any changes will be effective when they are posted to the site. Your continued use of our services following any change in terms of service will signify that you accept such changes. If you do not agree to any changes in the Terms of Service Agreement, please discontinue use of our services.

Violation of the Terms of Service is grounds for cancellation of service without refund and possible civil or criminal prosecution.

### Definitions:
1. **Charges**: means the applicable fees payable by you to Ottero in exchange for our products and services.
2. **Content**: means information published on the Ottero website or transmitted by us as part of the services we provide.

### Interpretation:
The provisions of the Terms of Service Agreement are in no way exhaustive. Any conduct that violates laws, regulations, or accepted norms of internet use and business practices is prohibited. Ottero reserves the right to limit or prohibit any of your activities that damage Ottero’s reputation as it pertains to your use of our services.

### Security:
- **1.1** You agree not to access or attempt to access any of our services by any means other than the Ottero interface.
- **1.2** You agree not to circumvent Ottero security features for any reason, particularly in order to avoid incurring charges.
- **1.3** You agree not to engage in activities that interrupt or interfere with Ottero services.
- **1.4** You agree not to use our services in a manner that unfairly, as determined by the sole discretion of Ottero, encumbers any system beyond those provided to you as part of the service you purchased.
- **1.5** You agree that your account is your sole responsibility and that you will accept the consequences of any breach of your obligations under this Terms of Service Agreement.
- **1.6** You agree that you are responsible for the security of your account and that you will keep your passwords confidential.
- **1.7** You agree that you are responsible for all activities that occur under your account.

### Privacy:
- **2.1** We take your privacy seriously and will take all reasonable measures to protect your personal information.
Any personal information received will only be used to fulfill your orders or provide you with services. We will not sell or redistribute your information to anyone without your consent.

### Service Availability:
- **3.1** We shall use our reasonable endeavors to make our services available to you at all times but shall not be liable for interruptions of service or downtime.
- **3.2** We reserve the right to suspend services at any time and for any reason, generally without notice. If such suspension lasts or is to last for more than two days, you will be notified of the reason.

### Payment:
- **4.1** You agree to pay all relevant charges incurred from your use of the services.
- **4.2** You authorize Ottero to automatically charge the amount you owe to the PayPal account or credit card you provided.
- **4.3** You agree that Ottero may cancel your account if any of your payments are delinquent.

### Limitations of Liability:
- **5.1** You expressly acknowledge and agree that, to the fullest extent permitted by applicable law, Ottero shall not be liable for any direct, indirect, incidental, special, consequential, or exemplary damages, including but not limited to damages for loss of profits, goodwill, use, data, or other intangible losses (even if Ottero has been advised of the possibility of such damages), resulting from:
- (i) the use or the inability to use the service;
- (ii) the cost of procurement of substitute goods and services resulting from any goods, data, information, or services purchased or obtained or transactions entered into through the service;
- (iii) unauthorized access to or alteration of your transmissions or data;
- (iv) statements or conduct of any third party on the service; or
- (v) any other matter relating to the service.
- **5.2** Nothing in these Terms of Service shall affect the statutory rights of any consumer, or exclude or restrict Ottero’s liability arising from fraud or other criminal action, or exclude or restrict any liability for death or personal injury arising from the negligence of Ottero.

### Disclaimer:
You expressly acknowledge and agree that:
- **6.1** Your use of the service is at your sole risk. The service is provided on an “as is” and “as available” basis. To the maximum extent permitted by law, Ottero disclaims all warranties, conditions, and other terms of any kind, whether express or implied, including but not limited to any implied term of merchantability, satisfactory quality, fitness for a particular purpose, or as to non-infringement of any intellectual property right.
- **6.2** Ottero makes no warranty or representation that (i) the service will meet your requirements, (ii) the service will be uninterrupted, timely, secure, or error-free, or (iii) any errors in the software will be corrected.

### Cancellation:
- **7.1** Ottero reserves the right to terminate your account at any time.
- **7.2** You agree that in order to cancel your service, you must log in to the client portal and request to cancel the service.
- **7.3** If you cancel your service before the end of your current paid month, your cancellation will take effect immediately, or at the end of the billing period, and you will not be billed again.

### Refund:
- **8.1** You agree that you have two days from the date of initial purchase to claim a refund.
- **8.2** You agree that you must provide a valid reason for requesting the refund within the two-day period from the date of purchase.
- **8.3** You agree that after the two-day period, you are not eligible to receive a refund for any service purchase or renewal.

### Proprietary Rights:
- **9.1** You agree that you have no right to use any of Ottero’s logos, trademarks, or domain names.

### Indemnity:
- **10.1** You agree to defend, hold harmless, and indemnify Ottero from any and all liability, claim, loss, judgment, damage, cost, or expense (including, without limitation, reasonable legal fees) arising out of your breach or violation of any covenant contained in this policy or resulting from your use of the services.

`;

const MarkdownRenderer = () => {
    return (
        <div className="markdown-content">
            <ReactMarkdown>
                {markdownContent}
            </ReactMarkdown>
        </div>
    );
};

export default MarkdownRenderer;