import type { Actions } from './$types';
import { tsaApi } from '../libs/server/ApiClientSupplier';
import { fail } from '@sveltejs/kit';

export const actions: Actions = {
	default: async ({ request }) => {
		const formData = await request.formData();
		const base64EncodedResponse = formData.get('response') as string;
		const x509Certificate = formData.get('certificate') as File;

		try {
			const decodedResponse = new Uint8Array(
				atob(base64EncodedResponse)
					.split('')
					.map((c) => c.charCodeAt(0))
			);

			if (x509Certificate.size) {
				return {
					success: true,
					validationResult: await tsaApi.validateWithCertificate({
						response: new Blob([decodedResponse]),
						x509Certificate: x509Certificate
					})
				};
			}

			return {
				success: true,
				validationResult: await tsaApi.validate({
					body: new Blob([decodedResponse])
				})
			};
		} catch (e) {
			// TODO: better error handling of client and server side errors
			return fail(400, {
				success: false
			});
		}
	}
} satisfies Actions;
