<script lang="ts">
	import type { ActionData, SubmitFunction } from './$types';
	import { Button, Fileupload, Input, Label } from 'flowbite-svelte';
	import { enhance } from '$app/forms';
	import { fade } from 'svelte/transition';
	import ValidationResultOverview from './ValidationResultOverview.svelte';

	export let form: ActionData;
	let submitted: boolean;
	$: formError = form && !form.success;

	const validate: SubmitFunction = () => {
		submitted = true;

		return async ({ update }) => {
			submitted = false;
			await update();
		};
	};
</script>

<!-- TODO: better error handling of client and server side errors -->
<div class="container mx-auto xl:px-48">
	<form method="POST" use:enhance={validate} enctype="multipart/form-data">
		<div class="pt-8">
			<div class="mb-6">
				<Label for="tsp-response" class="mb-2">TSP Response</Label>
				<Input
					id="tsp-response"
					name="response"
					type="text"
					placeholder="Base64 Encoded TSP Response"
					disabled={submitted}
					color={formError ? 'red' : 'base'}
					required
				/>
			</div>
			<div class="mb-6">
				<Label for="x509Certificate" class="pb-2">X.509 Certificate (optional)</Label>
				<Fileupload
					id="x509Certificate"
					name="certificate"
					class="mb-2"
					color={formError ? 'red' : 'base'}
				/>
			</div>
			<div class="mb-6">
				<Button color="primary" type="submit" disabled={submitted}>Validate</Button>
			</div>
		</div>
	</form>

	{#if form?.success}
		<div class="mt-14" transition:fade>
			<ValidationResultOverview validationResult={form.validationResult} />
		</div>
	{/if}
</div>
